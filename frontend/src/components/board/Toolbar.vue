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
             :title="getNameByHex(color)"
        ></div>
      </div>
    </div>

    <button @click="addTextElement" title="Add Text">Text</button>
    <button @click="addFrameElement" title="Add Frame">Frame</button>
    <button @click="addDomainModelItem" title="Add Domain Model Item">Model</button>

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
import { ElementType, DomainModelItemType, type FrameElement, type StickyNoteElement, type TextElement, type DomainModelItemElement } from '@/types/elements';
import { useRoute, useRouter } from 'vue-router';
import { stickyNoteApi, textBoxApi, frameApi, commonApi, domainModelItemApi } from '@/api';
import { STICKY_NOTE_COLOR_PALETTE, getNameByHex } from '@/constants/colors';

const route = useRoute();
const router = useRouter();
const boardStore = useBoardStore();
const historyStore = useHistoryStore();
const boardId = route.params.boardId as string;

const showColorPicker = ref(false);
const isAnalyzing = ref(false);
const stickyNoteColors = STICKY_NOTE_COLOR_PALETTE;

const STICKY_NOTE_WIDTH = 150;
const STICKY_NOTE_HEIGHT = 150;

const TEXT_ELEMENT_WIDTH = 200;
const TEXT_ELEMENT_HEIGHT = 40;

const FRAME_WIDTH = 360;
const FRAME_HEIGHT = 240;

const DOMAIN_MODEL_ITEM_WIDTH = 200;
const DOMAIN_MODEL_ITEM_HEIGHT = 150;

const getViewportCenterBoardPosition = (elementWidth: number, elementHeight: number) => {
  const { x, y, scale } = boardStore.canvasTransform;
  const viewportCenterX = window.innerWidth / 2;
  const viewportCenterY = window.innerHeight / 2;

  return {
    x: (viewportCenterX - x) / scale - elementWidth / 2,
    y: (viewportCenterY - y) / scale - elementHeight / 2,
  };
};

async function addStickyNoteApi(colorHex: string, position: { x: number; y: number }) {
  try {
    const data = await stickyNoteApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      geoX: STICKY_NOTE_WIDTH / boardStore.canvasTransform.scale,
      geoY: STICKY_NOTE_HEIGHT / boardStore.canvasTransform.scale,
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
  const position = getViewportCenterBoardPosition(STICKY_NOTE_WIDTH, STICKY_NOTE_HEIGHT);
  const createdId = await addStickyNoteApi(color, position);
  if (!createdId) return;

  const newStickyNote: Omit<StickyNoteElement, 'id'> = {
    type: ElementType.StickyNote,
    x: position.x,
    y: position.y,
    width: STICKY_NOTE_WIDTH / boardStore.canvasTransform.scale,
    height: STICKY_NOTE_HEIGHT / boardStore.canvasTransform.scale,
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

async function addTextElementApi(position: { x: number; y: number }) {
  try {
    const data = await textBoxApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      geoX: TEXT_ELEMENT_WIDTH / boardStore.canvasTransform.scale,
      geoY: TEXT_ELEMENT_HEIGHT / boardStore.canvasTransform.scale,
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
  const position = getViewportCenterBoardPosition(TEXT_ELEMENT_WIDTH, TEXT_ELEMENT_HEIGHT);
  const createdId = await addTextElementApi(position);
  if (!createdId) return;

  const newTextElement: Omit<TextElement, 'id'> = {
    type: ElementType.Text,
    x: position.x,
    y: position.y,
    width: TEXT_ELEMENT_WIDTH / boardStore.canvasTransform.scale,
    height: TEXT_ELEMENT_HEIGHT / boardStore.canvasTransform.scale,
    text: 'New Text',
    fontSize: 24,
    fontFamily: 'Arial',
    textColor: '#000000',
    draggable: true,
  };
  boardStore.addElement(newTextElement, createdId);
  historyStore.addState(); // Record state change
};

async function addFrameApi(position: { x: number; y: number }) {
  try {
    const data = await frameApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      width: FRAME_WIDTH / boardStore.canvasTransform.scale,
      height: FRAME_HEIGHT / boardStore.canvasTransform.scale,
      title: 'New Frame'
    });
    return data.id;
  } catch (error) {
    console.log('Error adding frame element:', error);
    return null;
  }
}

const addFrameElement = async () => {
  const position = getViewportCenterBoardPosition(FRAME_WIDTH, FRAME_HEIGHT);
  const createdId = await addFrameApi(position);

  const newFrameElement: Omit<FrameElement, 'id'> = {
    type: ElementType.Frame,
    x: position.x,
    y: position.y,
    width: FRAME_WIDTH / boardStore.canvasTransform.scale,
    height: FRAME_HEIGHT / boardStore.canvasTransform.scale,
    title: 'New Frame',
    fill: '#ffffff',
    stroke: '#5f6b7a',
    strokeWidth: 2,
    draggable: true,
  };

  boardStore.addElement(newFrameElement, createdId ?? undefined);
  historyStore.addState(); // Record state change
};

async function addDomainModelItemApi(position: { x: number; y: number }) {
  try {
    const data = await domainModelItemApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      width: DOMAIN_MODEL_ITEM_WIDTH / boardStore.canvasTransform.scale,
      height: DOMAIN_MODEL_ITEM_HEIGHT / boardStore.canvasTransform.scale,
      name: 'NewEntity',
      type: DomainModelItemType.ENTITY,
      attributes: [
        { name: 'id', dataType: 'UUID', constraint: 'PK', displayOrder: 0 }
      ]
    });
    return data.id;
  } catch (error) {
    console.log('Error adding domain model item:', error);
    return null;
  }
}

const addDomainModelItem = async () => {
  const position = getViewportCenterBoardPosition(DOMAIN_MODEL_ITEM_WIDTH, DOMAIN_MODEL_ITEM_HEIGHT);
  const createdId = await addDomainModelItemApi(position);
  if (!createdId) return;

  const newDomainModelItem: Omit<DomainModelItemElement, 'id'> = {
    type: ElementType.DomainModelItem,
    modelType: DomainModelItemType.ENTITY,
    x: position.x,
    y: position.y,
    width: DOMAIN_MODEL_ITEM_WIDTH / boardStore.canvasTransform.scale,
    height: DOMAIN_MODEL_ITEM_HEIGHT / boardStore.canvasTransform.scale,
    name: 'NewEntity',
    attributes: [
      { name: 'id', dataType: 'UUID', constraint: 'PK', displayOrder: 0 }
    ],
    draggable: true,
  };

  boardStore.addElement(newDomainModelItem, createdId);
  historyStore.addState();
};

const deleteSelectedElements = async () => {
  const idsToDelete = [...boardStore.selectedElementIds];
  const deleteResults = await Promise.allSettled(
    idsToDelete.map(async (id) => {
      const element = boardStore.getElementById(id);
      if (!element) return { id, success: false };

      if (element.type === ElementType.StickyNote) {
        await stickyNoteApi.delete(id);
        return { id, success: true };
      }

      if (element.type === ElementType.Text) {
        await textBoxApi.delete(id);
        return { id, success: true };
      }

      if (element.type === ElementType.Frame) {
        await frameApi.delete(id);
        return { id, success: true };
      }

      if (element.type === ElementType.DomainModelItem) {
        await domainModelItemApi.delete(id);
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