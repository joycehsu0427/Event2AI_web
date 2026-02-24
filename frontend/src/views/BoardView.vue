<template>
  <div class="board-view">
    <Toolbar class="board-toolbar" />
    <MiroBoard class="miro-board-container" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import Toolbar from '@/components/board/Toolbar.vue';
import MiroBoard from '@/components/board/MiroBoard.vue';
import { loadStateFromLocalStorage } from '@/utils/localStorage';

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

onMounted(() => {
  // Load board state from local storage
  const savedState = loadStateFromLocalStorage();
  if (savedState) {
    boardStore.loadBoardState(savedState);
  }
  // Initialize history with the current state (either loaded or default)
  historyStore.initializeHistory();

  // Add keyboard shortcuts for Undo/Redo (Ctrl+Z, Ctrl+Y)
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});

// Watch for changes in board elements or canvas transform to trigger auto-save
watch(
  () => boardStore.getCurrentBoardState(),
  (newState, oldState) => {
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
