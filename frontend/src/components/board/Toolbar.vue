<template>
  <div class="toolbar">
    <!-- Tools for adding elements -->
    <div class="sticky-tool-container">
      <button @click="showColorPicker = !showColorPicker" :class="{ active: showColorPicker }" title="Add Sticky Note">
        Sticky
      </button>
      
      <!-- Popover Color Picker -->
      <div v-if="showColorPicker" class="color-picker-popover">
        <div v-for="color in stickyNoteColors" :key="color"
             class="color-swatch"
             :style="{ backgroundColor: color }"
             @click="addStickyNote(color)"
             title="Pick color and add note"
        ></div>
      </div>
    </div>

    <button @click="addTextElement" title="Add Text">Text</button>
    <button @click="addFrameElement" title="Add Frame">Frame</button>

    <!-- Undo/Redo/Delete -->
    <div class="divider"></div>
    <button @click="historyStore.undo()" :disabled="!historyStore.canUndo" title="Undo">Undo</button>
    <button @click="historyStore.redo()" :disabled="!historyStore.canRedo" title="Redo">Redo</button>
    <button @click="deleteSelectedElements" :disabled="boardStore.selectedElementIds.length === 0" title="Delete Selected">Delete</button>
    <button @click="handleAnalysis" :disabled="isAnalyzing" title="Analyze Board">{{ isAnalyzing ? 'Analyzing...' : 'Analyze' }}</button>
    <div class="divider"></div>
    <button @click="goHome" title="Back to Home">Back</button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import { ElementType, type FrameElement, type StickyNoteElement, type TextElement } from '@/interfaces/elements';
import { useRoute, useRouter } from 'vue-router';
import { elementApi, commonApi } from '@/api';
import { STICKY_NOTE_COLOR_PALETTE, getNameByHex } from '@/constants/colors';

const route = useRoute();
const router = useRouter();
const boardStore = useBoardStore();
const historyStore = useHistoryStore();
const boardId = route.params.boardId as string;

const showColorPicker = ref(false);
const isAnalyzing = ref(false);
const stickyNoteColors = STICKY_NOTE_COLOR_PALETTE;

async function addStickyNoteApi(colorHex: string) {
  try {
    const data = await elementApi.createStickyNote({
      boardId: boardId,
      posX: 50,
      posY: 50,
      geoX: 150,
      geoY: 150,
      description: 'New Sticky Note',
      color: getNameByHex(colorHex), // Send color name (e.g., 'blue') to backend
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

const addStickyNote = async (color: string) => {
  const createdId = await addStickyNoteApi(color);
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
    backgroundColor: color,
    draggable: true,
  };
  boardStore.addElement(newStickyNote, createdId);
  historyStore.addState(); // Record state change
  
  // Close the picker after selection
  showColorPicker.value = false;
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
      color: 'yellow', // Default color name
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

const handleAnalysis = async () => {
  isAnalyzing.value = true;
  try {
    await commonApi.analysis(boardId);
    alert('Analysis completed successfully');
  } catch (error) {
    console.error('Analysis failed:', error);
    alert('Analysis failed. Please try again.');
  } finally {
    isAnalyzing.value = false;
  }
};

const goHome = () => {
  router.push({ name: 'home' });
};
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  padding: 10px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  align-items: center;
}

.sticky-tool-container {
  position: relative;
}

.color-picker-popover {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  background: white;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  gap: 8px;
  z-index: 1001;
}

.color-picker-popover::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 20px;
  width: 12px;
  height: 12px;
  background: white;
  transform: rotate(45deg);
}

.divider {
  width: 1px;
  height: 24px;
  background-color: #e0e0e0;
  margin: 0 5px;
}

.toolbar button {
  padding: 8px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background-color: #ffffff;
  color: #333;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.toolbar button:hover:not(:disabled) {
  background-color: #f5f5f5;
  border-color: #d0d0d0;
}

.toolbar button.active {
  background-color: #eef6ff;
  border-color: #007bff;
  color: #007bff;
}

.toolbar button:disabled {
  background-color: #fafafa;
  color: #ccc;
  cursor: not-allowed;
}

.color-swatch {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  border: 1px solid rgba(0,0,0,0.1);
  transition: transform 0.2s;
}

.color-swatch:hover {
  transform: scale(1.2);
}
</style>