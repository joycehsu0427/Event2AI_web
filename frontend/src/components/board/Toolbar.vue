<template>
  <div class="toolbar">
    <!-- Tools for adding elements -->
    <button @click="addStickyNote" title="Add Sticky Note">Sticky</button>
    <button @click="addTextElement" title="Add Text">Text</button>
    <button @click="addFrameElement" title="Add Frame">Frame</button>

    <!-- Color options for Sticky Notes -->
    <div class="color-palette">
      <div v-for="color in stickyNoteColors" :key="color"
           class="color-swatch"
           :style="{ backgroundColor: color, border: boardStore.defaultStickyNoteColor === color ? '2px solid #007bff' : '1px solid #ccc' }"
           @click="selectDefaultColor(color)"
           title="Set default sticky note color"
      ></div>
    </div>
    <button @click="applyColorToSelected" :disabled="!canApplyColorToSelected" title="Apply color to selected">Apply Color</button>


    <!-- Undo/Redo/Delete -->
    <button @click="historyStore.undo()" :disabled="!historyStore.canUndo" title="Undo">Undo</button>
    <button @click="historyStore.redo()" :disabled="!historyStore.canRedo" title="Redo">Redo</button>
    <button @click="deleteSelectedElements" :disabled="boardStore.selectedElementIds.length === 0" title="Delete Selected">Delete</button>
    <!-- Future: Zoom controls, Pan mode, etc. -->
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import { ElementType, type FrameElement, type StickyNoteElement, type TextElement } from '@/interfaces/elements';
import { useRoute } from 'vue-router';
import { elementApi } from '@/api';

const route = useRoute();
const boardStore = useBoardStore();
const historyStore = useHistoryStore();
const boardId = route.params.boardId as string;

const stickyNoteColors = ['#ffeb3b', '#c1e1c1', '#add8e6', '#ffb6c1', '#d3d3d3']; // Yellow, Green, Blue, Pink, Grey

async function addStickyNoteApi() {
  try {
    const data = await elementApi.createStickyNote({
      boardId: boardId,
      posX: 50,
      posY: 50,
      geoX: 150,
      geoY: 150,
      description: 'New Sticky Note',
      color: boardStore.getDefaultStickyNoteColor,
      tag: 'sticky-note',
      fontColor: '#000000',
      fontSize: 20
    });
    return data.id;
  } catch (error) {
    console.log('Error adding sticky note:', error);
    return null;
  }
}

const addStickyNote = async () => {
  const createdId = await addStickyNoteApi();
  if (!createdId) return;

  const newStickyNote: Omit<StickyNoteElement, 'id'> = {
    type: ElementType.StickyNote,
    x: 50, // Default position
    y: 50, // Default position
    width: 150,
    height: 150,
    text: 'New Sticky Note',
    fontSize: 20,
    textColor: '#000000',
    backgroundColor: boardStore.getDefaultStickyNoteColor, // Use default color from store
    draggable: true,
  };
  boardStore.addElement(newStickyNote, createdId);
  historyStore.addState(); // Record state change
};

async function addTextElementApi() {
  try {
    const data = await elementApi.createTextBox({
      boardId: boardId,
      posX: 250,
      posY: 50,
      geoX: 200,
      geoY: 40,
      description: 'New Text',
      color: boardStore.getDefaultStickyNoteColor,
      tag: 'text-box',
      fontColor: '#000000',
      fontSize: 24
    });
    return data.id;
  } catch (error) {
    console.log('Error adding text element:', error);
    return null;
  }
}

const addTextElement = async () => {
  const createdId = await addTextElementApi();
  if (!createdId) return;

  const newTextElement: Omit<TextElement, 'id'> = {
    type: ElementType.Text,
    x: 250, // Default position
    y: 50, // Default position
    width: 200,
    height: 40,
    text: 'New Text',
    fontSize: 24,
    fontFamily: 'Arial',
    textColor: '#000000',
    draggable: true,
  };
  boardStore.addElement(newTextElement, createdId);
  historyStore.addState(); // Record state change
};

async function addFrameApi() {
  try {
    const data = await elementApi.createFrame({
      boardId: boardId,
      posX: 450,
      posY: 80,
      width: 360,
      height: 240,
      title: 'New Frame'
    });
    return data.id;
  } catch (error) {
    console.log('Error adding frame element:', error);
    return null;
  }
}

const addFrameElement = async () => {
  const createdId = await addFrameApi();

  const newFrameElement: Omit<FrameElement, 'id'> = {
    type: ElementType.Frame,
    x: 450,
    y: 80,
    width: 360,
    height: 240,
    title: 'New Frame',
    fill: '#ffffff',
    stroke: '#5f6b7a',
    strokeWidth: 2,
    draggable: true,
  };

  boardStore.addElement(newFrameElement, createdId ?? undefined);
  historyStore.addState(); // Record state change
};

const deleteSelectedElements = async () => {
  const idsToDelete = [...boardStore.selectedElementIds];
  const deleteResults = await Promise.allSettled(
    idsToDelete.map(async (id) => {
      const element = boardStore.getElementById(id);
      if (!element) return { id, success: false };

      if (element.type === ElementType.StickyNote) {
        await elementApi.deleteStickyNote(id);
        return { id, success: true };
      }

      if (element.type === ElementType.Text) {
        await elementApi.deleteTextBox(id);
        return { id, success: true };
      }

      if (element.type === ElementType.Frame) {
        await elementApi.deleteFrame(id);
        return { id, success: true };
      }

      return { id, success: false };
    })
  );

  const successfulIds = deleteResults
    .filter((result): result is PromiseFulfilledResult<{ id: string; success: boolean }> => result.status === 'fulfilled')
    .filter((result) => result.value.success)
    .map((result) => result.value.id);

  if (successfulIds.length > 0) {
    boardStore.deleteElements(successfulIds);
    historyStore.addState(); // Record state change
  }

  deleteResults
    .filter((result): result is PromiseRejectedResult => result.status === 'rejected')
    .forEach((result) => {
      console.error('Error deleting selected element:', result.reason);
    });
};

const selectDefaultColor = (color: string) => {
  boardStore.setDefaultStickyNoteColor(color);
};

// Check if any selected elements are sticky notes to enable apply color button
const canApplyColorToSelected = computed(() => {
  return boardStore.getSelectedElements.some(el => el.type === ElementType.StickyNote);
});

const applyColorToSelected = () => {
  boardStore.updateSelectedElementsColor(boardStore.getDefaultStickyNoteColor);
  historyStore.addState(); // Record state change
};
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  padding: 10px;
  background-color: #f0f0f0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  align-items: center; /* Align items vertically */
}

.toolbar button {
  padding: 8px 15px;
  border: none;
  border-radius: 5px;
  background-color: #007bff;
  color: white;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap; /* Prevent button text from wrapping */
}

.toolbar button:hover:not(:disabled) {
  background-color: #0056b3;
}

.toolbar button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.color-palette {
  display: flex;
  gap: 5px;
  margin-left: 10px;
  margin-right: 5px;
}

.color-swatch {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: transform 0.1s ease-in-out;
}

.color-swatch:hover {
  transform: scale(1.1);
}
</style>