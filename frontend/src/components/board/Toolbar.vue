<template>
  <div class="toolbar">
    <!-- Tools for adding elements -->
    <button @click="addStickyNote" title="Add Sticky Note">Sticky</button>
    <button @click="addTextElement" title="Add Text">Text</button>

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
import { ElementType, StickyNoteElement, TextElement } from '@/interfaces/elements';

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const stickyNoteColors = ['#ffeb3b', '#c1e1c1', '#add8e6', '#ffb6c1', '#d3d3d3']; // Yellow, Green, Blue, Pink, Grey

const addStickyNote = () => {
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
  boardStore.addElement(newStickyNote);
  historyStore.addState(); // Record state change
};

const addTextElement = () => {
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
  boardStore.addElement(newTextElement);
  historyStore.addState(); // Record state change
};

const deleteSelectedElements = () => {
  boardStore.deleteElements(boardStore.selectedElementIds);
  historyStore.addState(); // Record state change
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