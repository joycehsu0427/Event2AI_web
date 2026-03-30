<template>
  <div class="board-view">
    <Toolbar class="board-toolbar" />
    <MiroBoard class="miro-board-container" />
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import { onMounted, onUnmounted, watch } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import type { BoardStoreState } from '@/stores/boardStore';
import { ElementType, type BoardElement } from '@/interfaces/elements';
import { useHistoryStore } from '@/stores/historyStore';
import { useTimerStore } from '@/stores/timerStore';
import Toolbar from '@/components/board/Toolbar.vue';
import MiroBoard from '@/components/board/MiroBoard.vue';
import { loadStateFromLocalStorage } from '@/utils/localStorage';
import axios from 'axios';

const route = useRoute();
const boardId = route.params.boardId as string;
const token = localStorage.getItem('token');
const boardStore = useBoardStore();
const historyStore = useHistoryStore();
const timerStore = useTimerStore();
const POLLING_INTERVAL_MS = 5000;

async function fetchBoardData(boardId: string) {
  try {
    const res = await axios.get(`http://localhost:8080/api/boards/${boardId}/components`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    const stickyNoteElements: BoardElement[] = (res.data?.stickyNotes ?? []).map((note: any) => ({
      id: note.id,
      type: ElementType.StickyNote,
      x: note.posX,
      y: note.posY,
      width: note.geoX,
      height: note.geoY,
      text: note.description,
      fontSize: Number(note.fontSize) || 20,
      textColor: note.fontColor || '#000000',
      backgroundColor: note.color || '#ffeb3b',
      draggable: true,
    }));

    const textElements: BoardElement[] = (res.data?.textBoxes ?? []).map((text: any) => ({
      id: text.id,
      type: ElementType.Text,
      x: text.posX,
      y: text.posY,
      width: text.geoX,
      height: text.geoY,
      text: text.description,
      fontSize: Number(text.fontSize) || 24,
      fontFamily: 'Arial',
      textColor: text.fontColor || '#000000',
      draggable: true,
    }));

    const state: BoardStoreState = {
      elements: [...stickyNoteElements, ...textElements],
      selectedElementIds: [],
      canvasTransform: { x: 0, y: 0, scale: 1 },
      editingElementId: null,
      defaultStickyNoteColor: '#ffeb3b'
    };
    boardStore.loadBoardState(state);
  } catch (error) {
    console.error('Error fetching board data:', error);
  }
}

onMounted(() => {
  fetchBoardData(boardId);
  // Load board state from local storage
  const savedState = loadStateFromLocalStorage();
  if (savedState) {
    boardStore.loadBoardState(savedState);
  }
  // Initialize history with the current state (either loaded or default)
  historyStore.initializeHistory();

  // Add keyboard shortcuts for Undo/Redo (Ctrl+Z, Ctrl+Y)
  window.addEventListener('keydown', handleKeyDown);

  // Poll board data in the background using the shared timer store.
  timerStore.start(() => fetchBoardData(boardId), POLLING_INTERVAL_MS);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
  timerStore.stop();
});

// Watch for changes in board elements or canvas transform to trigger auto-save
watch(
  () => boardStore.getCurrentBoardState(),
  () => {
    // Only save if the state has actually changed (deep comparison is implicitly handled by getCurrentBoardState creating a new object)
    // and if the change isn't due to history application
    if (historyStore.isApplyingHistory) {
      return;
    }
    boardStore.saveBoardStateToLocalStorage();
  },
  { deep: true } // Deep watch to detect changes within elements and canvasTransform
);


const handleKeyDown = (event: KeyboardEvent) => {
  if (event.ctrlKey || event.metaKey) { // Ctrl for Windows/Linux, Meta (Command) for Mac
    if (event.key === 'z') {
      event.preventDefault(); // Prevent browser undo
      historyStore.undo();
    }
    if (event.key === 'y') {
      event.preventDefault(); // Prevent browser redo
      historyStore.redo();
    }
  }
};
</script>

<style scoped>
.board-view {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  overflow: hidden; /* Ensure no scrollbars on the main view */
}

.board-toolbar {
  /* Styling for the toolbar, e.g., position, background */
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  background-color: #f0f0f0;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.miro-board-container {
  flex-grow: 1; /* MiroBoard takes up remaining space */
  background-color: #f5f5f5; /* Light background for the canvas */
}
</style>
