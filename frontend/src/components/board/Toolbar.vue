<template>
  <div class="toolbar">
    <!-- 1. Sticky Note Tool -->
    <div class="tool-container">
      <button @click="toggleColorPicker" :class="{ active: showColorPicker }" title="Add Sticky Note">
        Sticky
      </button>
      
      <div v-if="showColorPicker" class="popover color-picker-popover">
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
    <button @click="addEventStormingTemplate" :disabled="isCreatingTemplate" title="Insert Event Storming Template">
      {{ isCreatingTemplate ? 'Adding...' : 'Template' }}
    </button>

    <!-- 2. Domain Model Tool -->
    <div class="tool-container">
      <button @click="toggleModelTypePicker" :class="{ active: showModelTypePicker }" title="Add Domain Model">
        Model
      </button>

      <div v-if="showModelTypePicker" class="popover model-type-popover">
        <div v-for="type in modelTypes" :key="type"
             class="type-item"
             @click="addDomainModelItem(type)"
        >
          {{ type }}
        </div>
      </div>
    </div>

    <!-- 3. Connector Tool -->
    <button 
      @click="toggleLineTool" 
      :class="{ active: boardStore.isDrawingConnector }" 
      title="Draw Connection Line"
    >
      Line
    </button>

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
import { ref } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import { ElementType, DomainModelItemType, type BoardElement, type FrameElement, type StickyNoteElement, type TextElement, type DomainModelItemElement, ConnectorTargetType } from '@/types/elements';
import { useRoute, useRouter } from 'vue-router';
import { stickyNoteApi, textBoxApi, frameApi, commonApi, domainModelItemApi, type EventStormingTemplateResponse } from '@/api';
import { STICKY_NOTE_COLOR_PALETTE, getNameByHex, getHexByName } from '@/constants/colors';

const route = useRoute();
const router = useRouter();
const boardStore = useBoardStore();
const historyStore = useHistoryStore();
const boardId = route.params.boardId as string;

const showColorPicker = ref(false);
const showModelTypePicker = ref(false);
const isAnalyzing = ref(false);
const isCreatingTemplate = ref(false);

const stickyNoteColors = STICKY_NOTE_COLOR_PALETTE;
const modelTypes = Object.values(DomainModelItemType);

const STICKY_NOTE_WIDTH = 150;
const STICKY_NOTE_HEIGHT = 150;
const TEXT_ELEMENT_WIDTH = 200;
const TEXT_ELEMENT_HEIGHT = 40;
const FRAME_WIDTH = 360;
const FRAME_HEIGHT = 240;
const DOMAIN_MODEL_ITEM_WIDTH = 200;
const DOMAIN_MODEL_ITEM_HEIGHT = 150;

const toggleColorPicker = () => {
  showColorPicker.value = !showColorPicker.value;
  showModelTypePicker.value = false;
  boardStore.stopDrawingConnector();
};

const toggleModelTypePicker = () => {
  showModelTypePicker.value = !showModelTypePicker.value;
  showColorPicker.value = false;
  boardStore.stopDrawingConnector();
};

const toggleLineTool = () => {
  if (boardStore.isDrawingConnector) {
    boardStore.stopDrawingConnector();
    document.body.style.cursor = 'default';
  } else {
    showColorPicker.value = false;
    showModelTypePicker.value = false;
    boardStore.startDrawingConnector({ targetType: ConnectorTargetType.FREE_POINT });
    document.body.style.cursor = 'crosshair';
  }
};

const getViewportCenterBoardPosition = (elementWidth: number, elementHeight: number) => {
  const { x, y, scale } = boardStore.canvasTransform;
  const viewportCenterX = window.innerWidth / 2;
  const viewportCenterY = window.innerHeight / 2;

  return {
    x: (viewportCenterX - x) / scale - elementWidth / 2,
    y: (viewportCenterY - y) / scale - elementHeight / 2,
  };
};

const getViewportCenterBoardPoint = () => {
  const { x, y, scale } = boardStore.canvasTransform;
  const viewportCenterX = window.innerWidth / 2;
  const viewportCenterY = window.innerHeight / 2;

  return {
    x: (viewportCenterX - x) / scale,
    y: (viewportCenterY - y) / scale,
  };
};

const appendTemplateElements = (response: EventStormingTemplateResponse) => {
  const templateElements: Array<{ id: string; element: Omit<BoardElement, 'id'> }> = [
    ...(response.stickyNotes ?? []).map((note) => ({
      id: note.id,
      element: {
        type: ElementType.StickyNote,
        x: note.posX,
        y: note.posY,
        width: note.geoX,
        height: note.geoY,
        frameId: note.frameId ?? null,
        text: note.description,
        fontSize: Number(note.fontSize) || 20,
        textColor: note.fontColor || '#000000',
        backgroundColor: getHexByName(note.color),
        draggable: true,
      } satisfies Omit<StickyNoteElement, 'id'>,
    })),
    ...(response.textBoxes ?? []).map((text) => ({
      id: text.id,
      element: {
        type: ElementType.Text,
        x: text.posX,
        y: text.posY,
        width: text.geoX,
        height: text.geoY,
        frameId: text.frameId ?? null,
        text: text.description,
        fontSize: Number(text.fontSize) || 24,
        fontFamily: 'Arial',
        textColor: text.fontColor || '#000000',
        draggable: true,
      } satisfies Omit<TextElement, 'id'>,
    })),
    ...(response.frames ?? []).map((frame) => ({
      id: frame.id,
      element: {
        type: ElementType.Frame,
        x: frame.posX,
        y: frame.posY,
        width: frame.width,
        height: frame.height,
        title: frame.title,
        draggable: true,
      } satisfies Omit<FrameElement, 'id'>,
    })),
    ...(response.domainModelItems ?? []).map((item) => ({
      id: item.id,
      element: {
        type: ElementType.DomainModelItem,
        modelType: item.type as DomainModelItemType,
        x: item.posX,
        y: item.posY,
        width: item.width,
        height: item.height,
        name: item.name,
        attributes: item.attributes ?? [],
        draggable: true,
      } satisfies Omit<DomainModelItemElement, 'id'>,
    })),
  ];

  let selectedId: string | null = response.frames?.[0]?.id ?? templateElements[0]?.id ?? null;

  templateElements.forEach(({ id, element }) => {
    boardStore.addElement(element, id, false);
  });

  if (selectedId) {
    boardStore.selectElement(selectedId);
  }
};

async function addStickyNoteApi(colorHex: string, position: { x: number; y: number }) {
  try {
    const data = await stickyNoteApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      geoX: STICKY_NOTE_WIDTH,
      geoY: STICKY_NOTE_HEIGHT,
      description: 'New Sticky Note',
      color: getNameByHex(colorHex),
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
    width: STICKY_NOTE_WIDTH,
    height: STICKY_NOTE_HEIGHT,
    text: 'New Sticky Note',
    fontSize: 20,
    textColor: '#000000',
    backgroundColor: color,
    draggable: true,
  };
  boardStore.addElement(newStickyNote, createdId);
  historyStore.addState();
  showColorPicker.value = false;
};

async function addTextElementApi(position: { x: number; y: number }) {
  try {
    const data = await textBoxApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      geoX: TEXT_ELEMENT_WIDTH,
      geoY: TEXT_ELEMENT_HEIGHT,
      description: 'New Text',
      color: 'yellow',
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
    width: TEXT_ELEMENT_WIDTH,
    height: TEXT_ELEMENT_HEIGHT,
    text: 'New Text',
    fontSize: 24,
    fontFamily: 'Arial',
    textColor: '#000000',
    draggable: true,
  };
  boardStore.addElement(newTextElement, createdId);
  historyStore.addState();
};

async function addFrameApi(position: { x: number; y: number }) {
  try {
    const data = await frameApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      width: FRAME_WIDTH,
      height: FRAME_HEIGHT,
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
    width: FRAME_WIDTH,
    height: FRAME_HEIGHT,
    title: 'New Frame',
    fill: '#ffffff',
    stroke: '#5f6b7a',
    strokeWidth: 2,
    draggable: true,
  };

  boardStore.addElement(newFrameElement, createdId ?? undefined);
  historyStore.addState();
};

const addEventStormingTemplate = async () => {
  showColorPicker.value = false;
  showModelTypePicker.value = false;
  boardStore.stopDrawingConnector();

  const position = getViewportCenterBoardPoint();
  isCreatingTemplate.value = true;

  try {
    const response = await frameApi.createEventStormingTemplate({
      boardId,
      posX: position.x,
      posY: position.y,
    });

    if (response.boardId && response.boardId !== boardId) {
      console.warn('Event storming template response boardId does not match current board');
    }

    appendTemplateElements(response);
    historyStore.addState();
  } catch (error) {
    console.error('Error adding event storming template:', error);
  } finally {
    isCreatingTemplate.value = false;
  }
};

async function addDomainModelItemApi(type: DomainModelItemType, position: { x: number; y: number }) {
  try {
    const data = await domainModelItemApi.create({
      boardId: boardId,
      posX: position.x,
      posY: position.y,
      width: DOMAIN_MODEL_ITEM_WIDTH,
      height: DOMAIN_MODEL_ITEM_HEIGHT,
      name: `New${type}`,
      type: type,
      attributes: [
        { name: 'id', dataType: type === DomainModelItemType.ENUM ? '' : 'UUID', constraint: type === DomainModelItemType.ENUM ? '' : 'PK', displayOrder: 0 }
      ]
    });
    return data.id;
  } catch (error) {
    console.log('Error adding domain model item:', error);
    return null;
  }
}

const addDomainModelItem = async (type: DomainModelItemType) => {
  const position = getViewportCenterBoardPosition(DOMAIN_MODEL_ITEM_WIDTH, DOMAIN_MODEL_ITEM_HEIGHT);
  const createdId = await addDomainModelItemApi(type, position);
  if (!createdId) return;

  const newDomainModelItem: Omit<DomainModelItemElement, 'id'> = {
    type: ElementType.DomainModelItem,
    modelType: type,
    x: position.x,
    y: position.y,
    width: DOMAIN_MODEL_ITEM_WIDTH,
    height: DOMAIN_MODEL_ITEM_HEIGHT,
    name: `New${type}`,
    attributes: [
      { name: 'id', dataType: type === DomainModelItemType.ENUM ? '' : 'UUID', constraint: type === DomainModelItemType.ENUM ? '' : 'PK', displayOrder: 0 }
    ],
    draggable: true,
  };

  boardStore.addElement(newDomainModelItem, createdId);
  historyStore.addState();
  showModelTypePicker.value = false;
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
    historyStore.addState();
  }
};

type AnalysisItem = {
  useCaseName: string;
  [key: string]: unknown;
};

const sanitizeFileName = (value: string) => {
  const normalized = value
    .trim()
    .replace(/[<>:"/\\|?*\x00-\x1F]/g, '_')
    .replace(/\s+/g, '_');

  return normalized.slice(0, 120) || 'use_case';
};

const downloadJsonFile = (fileName: string, payload: unknown) => {
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = url;
  link.download = `${fileName}.json`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

const downloadAnalysisResults = (results: AnalysisItem[]) => {
  const duplicateCounter = new Map<string, number>();

  results.forEach((item, index) => {
    const baseName = sanitizeFileName(item.useCaseName || `use_case_${index + 1}`);
    const seenCount = duplicateCounter.get(baseName) ?? 0;
    const fileName = seenCount === 0 ? baseName : `${baseName}_${seenCount + 1}`;

    duplicateCounter.set(baseName, seenCount + 1);
    downloadJsonFile(fileName, item);
  });
};

const handleAnalysis = async () => {
  isAnalyzing.value = true;
  try {
    const response = await commonApi.analysis(boardId);

    if (!Array.isArray(response)) {
      throw new Error('Unexpected analysis response format: expected an array.');
    }

    const items = response.filter(
      (entry): entry is AnalysisItem =>
        typeof entry === 'object' && entry !== null && typeof (entry as { useCaseName?: unknown }).useCaseName === 'string'
    );

    if (items.length === 0) {
      alert('Analysis completed, but no use case data was returned.');
      return;
    }

    downloadAnalysisResults(items);
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

.tool-container {
  position: relative;
}

.popover {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  background: white;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  z-index: 1001;
}

.popover::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 20px;
  width: 12px;
  height: 12px;
  background: white;
  transform: rotate(45deg);
}

.color-picker-popover {
  gap: 8px;
}

.model-type-popover {
  flex-direction: column;
  min-width: 140px;
  padding: 5px;
}

.type-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #444;
  transition: background 0.2s;
}

.type-item:hover {
  background: #f0f7ff;
  color: #007bff;
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
