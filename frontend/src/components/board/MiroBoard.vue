<template>
  <div ref="containerRef" class="miro-board" @contextmenu.prevent>
    <v-stage
      ref="stageRef"
      :config="stageConfig"
      @wheel="handleWheel"
      @mousedown="handleStageMouseDown"
      @mouseup="handleStageMouseUp"
      @mousemove="handleStageMouseMove"
    >
      <v-layer>
        <!-- Connectors -->
        <template v-for="element in connectorElements" :key="element.id">
          <Connector :element="(element as ConnectorElement)" />
        </template>

        <!-- Elements -->
        <template v-for="element in nonConnectorElements" :key="element.id">
          <BoardElement :element="element" @transformend="handleTransformEnd" />
        </template>

        <v-transformer ref="transformerRef" :config="{ rotateEnabled: false }" />

        <v-rect
          v-if="isSelecting"
          :config="selectionRectConfig"
        />

        <!-- Preview line while drawing connector (Visible ONLY after first click) -->
        <v-arrow
          v-if="boardStore.isDrawingConnector && hasStartedDrawing && previewLinePoints.length >= 4"
          :config="previewLineConfig"
        />
      </v-layer>
    </v-stage>

    <!-- Floating Mini-Toolbar for Sticky Notes -->
    <div v-if="showFloatingToolbar" class="floating-toolbar" :style="floatingToolbarStyle">
      <div v-for="color in stickyNoteColors" :key="color"
           class="mini-color-swatch"
           :style="{ backgroundColor: color }"
           @click="boardStore.updateSelectedElementsColor(color)"
           :title="getNameByHex(color)"
      ></div>
    </div>

    <!-- Floating Mini-Toolbar for Connectors -->
    <div v-if="showConnectorToolbar" class="floating-toolbar connector-toolbar" :style="connectorToolbarStyle">
      <button @click="toggleConnectorDashed" title="Toggle Dashed Line">
        {{ isCurrentConnectorDashed ? 'Solid' : 'Dashed' }}
      </button>
      <div class="divider"></div>
      <div v-for="color in lineColors" :key="color"
           class="mini-color-swatch"
           :style="{ backgroundColor: color }"
           @click="updateConnectorColor(color)"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import type { Stage as KonvaStage } from 'konva/lib/Stage';
import type { Node as KonvaNode } from 'konva/lib/Node';
import type { KonvaMouseEvent } from 'konva/lib/Node';
import type { Transformer } from 'konva/lib/shapes/Transformer';
import BoardElement from './elements/BoardElement.vue';
import Connector from './elements/Connector.vue';
import { ElementType, ConnectorTargetType, ConnectorAnchorSide, ConnectorLineType, ConnectorArrowType, type ConnectorElement } from '@/types/elements';
import { STICKY_NOTE_COLOR_PALETTE, getNameByHex } from '@/constants/colors';
import { connectorApi } from '@/api';

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const containerRef = ref<HTMLDivElement | null>(null);
const stageRef = ref<{ getStage: () => KonvaStage } | null>(null);
const transformerRef = ref<Transformer | null>(null);

const stickyNoteColors = STICKY_NOTE_COLOR_PALETTE;
const lineColors = ['#333333', '#ff4d4f', '#52c41a', '#1890ff', '#faad14'];

const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: false,
  x: computed(() => boardStore.canvasTransform.x),
  y: computed(() => boardStore.canvasTransform.y),
  scaleX: computed(() => boardStore.canvasTransform.scale),
  scaleY: computed(() => boardStore.canvasTransform.scale),
});

const connectorElements = computed(() => 
  boardStore.getElements.filter(el => el.type === ElementType.Connector)
);

const nonConnectorElements = computed(() => {
  const layerPriority = {
    [ElementType.Frame]: 0,
    [ElementType.Text]: 1,
    [ElementType.StickyNote]: 2,
    [ElementType.DomainModelItem]: 2,
  } as any;

  return boardStore.getElements
    .filter(el => el.type !== ElementType.Connector)
    .sort((a, b) => (layerPriority[a.type] || 0) - (layerPriority[b.type] || 0));
});

// --- Connector Preview Logic ---
const currentMousePos = reactive({ x: 0, y: 0 });

const hasStartedDrawing = computed(() => {
  return boardStore.connectorDrawingStart?.targetId !== undefined || 
         boardStore.connectorDrawingStart?.point !== undefined;
});

const previewLinePoints = computed(() => {
  if (!boardStore.isDrawingConnector || !boardStore.connectorDrawingStart) return [];
  
  let startX = 0, startY = 0;
  const start = boardStore.connectorDrawingStart;

  if (start.targetId) {
    const el = boardStore.getElementById(start.targetId);
    if (el) {
      startX = el.x + el.width / 2;
      startY = el.y + el.height / 2;
    }
  } else if (start.point) {
    startX = start.point.x;
    startY = start.point.y;
  } else {
    return []; // No start defined yet
  }

  return [startX, startY, currentMousePos.x, currentMousePos.y];
});

const previewLineConfig = computed(() => ({
  points: previewLinePoints.value,
  stroke: '#007bff',
  strokeWidth: 2,
  dash: [5, 5],
  pointerLength: 10,
  pointerWidth: 10,
  fill: '#007bff',
  opacity: 0.6,
}));

// --- Connector Toolbar Logic ---
const showConnectorToolbar = computed(() => {
  if (boardStore.selectedElementIds.length !== 1) return false;
  const selectedId = boardStore.selectedElementIds[0];
  const element = boardStore.getElementById(selectedId);
  return element?.type === ElementType.Connector;
});

const isCurrentConnectorDashed = computed(() => {
  const selectedId = boardStore.selectedElementIds[0];
  const element = boardStore.getElementById(selectedId) as ConnectorElement | undefined;
  return element?.dashed || false;
});

const connectorToolbarStyle = computed(() => {
  if (boardStore.selectedElementIds.length !== 1) return {};
  return {
    position: 'absolute' as const,
    left: '50%',
    top: '80px',
    transform: 'translateX(-50%)',
    zIndex: 1002,
  };
});

const toggleConnectorDashed = () => {
  const id = boardStore.selectedElementIds[0];
  const el = boardStore.getElementById(id) as ConnectorElement;
  boardStore.updateElement(id, { dashed: !el.dashed });
};

const updateConnectorColor = (color: string) => {
  const id = boardStore.selectedElementIds[0];
  boardStore.updateElement(id, { strokeColor: color });
};

// --- Floating Toolbar Logic ---
const showFloatingToolbar = computed(() => {
  if (boardStore.selectedElementIds.length !== 1) return false;
  const selectedId = boardStore.selectedElementIds[0];
  const element = boardStore.getElementById(selectedId);
  return element?.type === ElementType.StickyNote;
});

const floatingToolbarStyle = computed(() => {
  if (boardStore.selectedElementIds.length !== 1) return {};
  const selectedId = boardStore.selectedElementIds[0];
  const element = boardStore.getElementById(selectedId);
  if (!element) return {};
  const { x, y, scale } = boardStore.canvasTransform;
  const left = (element.x * scale) + x + (element.width * scale / 2);
  const top = (element.y * scale) + y - 100;
  return {
    position: 'absolute' as const,
    left: `${left}px`,
    top: `${top}px`,
    zIndex: 1002,
  };
});

watch(() => boardStore.elements, () => {
  historyStore.addState();
}, { deep: true });

watch(() => boardStore.getEditingElementId, (editingElementId) => {
  const stageContainer = stageRef.value?.getStage().container();
  if (stageContainer) {
    const konvaContentDiv = stageContainer.parentElement;
    if (konvaContentDiv) {
      konvaContentDiv.style.zIndex = editingElementId !== null ? '0' : '1';
    }
  }
});

watch(() => boardStore.selectedElementIds, async (newSelection) => {
  await nextTick(); // Wait for Vue to render the new Konva elements
  const transformer = transformerRef.value?.getNode();
  const stage = stageRef.value?.getStage();
  if (!transformer || !stage) return;

  if (newSelection.length > 0) {
    const selectedNodes: KonvaNode[] = [];
    newSelection.forEach(id => {
      const node = stage.find(`#${id}`)[0];
      if (node && (node.attrs as any).type !== ElementType.Connector) {
        selectedNodes.push(node);
      }
    });
    transformer.nodes(selectedNodes);
  } else {
    transformer.nodes([]);
  }
  transformer.getLayer()?.batchDraw();
}, { immediate: true, deep: true });

const handleTransformEnd = (e: any) => {
  const node = e.target;
  const scaleX = node.scaleX();
  const scaleY = node.scaleY();
  const rotation = node.rotation();
  node.scaleX(1);
  node.scaleY(1);
  node.rotation(0);
  const elementId = node.id();
  if (elementId) {
    boardStore.updateElement(elementId, {
      x: node.x(),
      y: node.y(),
      width: Math.max(5, node.width() * scaleX),
      height: Math.max(5, node.height() * scaleY),
      rotation: rotation,
    });
  }
};

const updateStageSize = () => {
  if (containerRef.value) {
    stageConfig.width = containerRef.value.offsetWidth;
    stageConfig.height = containerRef.value.offsetHeight;
  }
};

onMounted(() => {
  updateStageSize();
  window.addEventListener('resize', updateStageSize);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateStageSize);
});

const isPanning = ref(false);
const lastPointerPosition = reactive({ x: 0, y: 0 });
const isSelecting = ref(false);
const selectionStart = reactive({ x: 0, y: 0 });
const selectionRectConfig = reactive({
  x: 0,
  y: 0,
  width: 0,
  height: 0,
  fill: 'rgba(0, 162, 255, 0.1)',
  stroke: 'rgba(0, 162, 255, 0.5)',
  strokeWidth: 1,
  dash: [5, 5],
});

const handleStageMouseDown = (e: KonvaMouseEvent) => {
  if (boardStore.getEditingElementId) return;
  const stage = e.target.getStage();
  if (!stage) return;
  const pointer = stage.getPointerPosition();
  if (!pointer) return;
  const stageX = (pointer.x - stage.x()) / stage.scaleX();
  const stageY = (pointer.y - stage.y()) / stage.scaleY();

  if (boardStore.isDrawingConnector) {
    handleConnectorMouseDown(e, stageX, stageY);
    return;
  }

  if (e.target === stage) {
    if (e.evt.button === 2) {
      e.evt.preventDefault();
      isPanning.value = true;
      lastPointerPosition.x = e.evt.clientX;
      lastPointerPosition.y = e.evt.clientY;
    }
    else if (e.evt.button === 0) {
      boardStore.selectElement(null);
      isSelecting.value = true;
      selectionStart.x = stageX;
      selectionStart.y = stageY;
      selectionRectConfig.x = stageX;
      selectionRectConfig.y = stageY;
      selectionRectConfig.width = 0;
      selectionRectConfig.height = 0;
    }
  }
};

const handleConnectorMouseDown = async (e: KonvaMouseEvent, stageX: number, stageY: number) => {
  const stage = stageRef.value?.getStage();
  if (!stage) return;
  const target = e.target;
  const isElement = target !== stage;
  
  let targetId = isElement ? target.id() : undefined;
  let targetType = ConnectorTargetType.FREE_POINT;
  
  if (isElement) {
    const node = target;
    const elementType = (node.attrs as any).type || (node.parent?.attrs as any)?.type;
    targetType = mapElementTypeToConnectorTargetType(elementType);
    if (!targetId) targetId = node.parent?.id();
  }

  if (!boardStore.connectorDrawingStart?.targetId && !boardStore.connectorDrawingStart?.point) {
    boardStore.startDrawingConnector({
      targetId: targetId,
      targetType: targetType,
      point: !targetId ? { x: stageX, y: stageY } : undefined
    });
  } else {
    const start = boardStore.connectorDrawingStart;
    const end = {
      targetId: targetId,
      targetType: targetType,
      point: !targetId ? { x: stageX, y: stageY } : undefined
    };

    try {
      const boardId = boardStore.getCurrentBoardId();
      if (!boardId) return;
      const newConnectorData = {
        boardId: boardId,
        fromTargetType: start.targetType,
        fromTargetId: start.targetId,
        fromSide: start.targetId ? ConnectorAnchorSide.RIGHT : undefined,
        fromPoint: start.point,
        toTargetType: end.targetType,
        toTargetId: end.targetId,
        toSide: end.targetId ? ConnectorAnchorSide.LEFT : undefined,
        toPoint: end.point,
        lineType: ConnectorLineType.STRAIGHT,
        strokeColor: '#333333',
        strokeWidth: 2,
        dashed: false,
        startArrow: ConnectorArrowType.NONE,
        endArrow: ConnectorArrowType.TRIANGLE,
        zIndex: 0
      };
      const res = await connectorApi.create(newConnectorData);
      boardStore.addElement({
        ...newConnectorData,
        type: ElementType.Connector,
      } as any, res.id);
      boardStore.stopDrawingConnector();
      document.body.style.cursor = 'default';
      historyStore.addState();
    } catch (err) {
      console.error('Failed to create connector:', err);
    }
  }
};

const mapElementTypeToConnectorTargetType = (type: string): ConnectorTargetType => {
  switch(type) {
    case ElementType.StickyNote: return ConnectorTargetType.STICKY_NOTE;
    case ElementType.Text: return ConnectorTargetType.TEXT_BOX;
    case ElementType.Frame: return ConnectorTargetType.FRAME;
    case ElementType.DomainModelItem: return ConnectorTargetType.DOMAIN_MODEL_ITEM;
    default: return ConnectorTargetType.FREE_POINT;
  }
};

const handleStageMouseUp = () => {
  isPanning.value = false;
  isSelecting.value = false;
};

const handleStageMouseMove = (e: KonvaMouseEvent) => {
  if (boardStore.getEditingElementId) return;
  const stage = e.target.getStage();
  if (!stage) return;
  const pointer = stage.getPointerPosition();
  if (!pointer) return;
  const stageX = (pointer.x - stage.x()) / stage.scaleX();
  const stageY = (pointer.y - stage.y()) / stage.scaleY();
  currentMousePos.x = stageX;
  currentMousePos.y = stageY;

  if (isPanning.value) {
    const dx = e.evt.clientX - lastPointerPosition.x;
    const dy = e.evt.clientY - lastPointerPosition.y;
    boardStore.setCanvasTransform({
      x: boardStore.canvasTransform.x + dx,
      y: boardStore.canvasTransform.y + dy
    });
    lastPointerPosition.x = e.evt.clientX;
    lastPointerPosition.y = e.evt.clientY;
    return;
  }

  if (isSelecting.value) {
    selectionRectConfig.x = Math.min(stageX, selectionStart.x);
    selectionRectConfig.y = Math.min(stageY, selectionStart.y);
    selectionRectConfig.width = Math.abs(stageX - selectionStart.x);
    selectionRectConfig.height = Math.abs(stageY - selectionStart.y);
    const selectedIds: string[] = [];
    boardStore.getElements.forEach((el) => {
      if (el.type === ElementType.Connector) return;
      if (
        selectionRectConfig.x < el.x + (el.width || 0) &&
        selectionRectConfig.x + selectionRectConfig.width > el.x &&
        selectionRectConfig.y < el.y + (el.height || 0) &&
        selectionRectConfig.y + selectionRectConfig.height > el.y
      ) {
        selectedIds.push(el.id);
      }
    });
    boardStore.selectedElementIds = selectedIds;
  }
};

const ZOOM_FACTOR = 1.1;
const handleWheel = (e: KonvaMouseEvent) => {
  e.evt.preventDefault();
  const stage = stageRef.value?.getStage();
  if (!stage) return;
  const oldScale = boardStore.canvasTransform.scale;
  const pointer = stage.getPointerPosition();
  if (!pointer) return;
  const mousePointTo = {
    x: (pointer.x - stage.x()) / oldScale,
    y: (pointer.y - stage.y()) / oldScale,
  };
  const newScale = e.evt.deltaY > 0 ? oldScale / ZOOM_FACTOR : oldScale * ZOOM_FACTOR;
  boardStore.setCanvasTransform({
    x: pointer.x - mousePointTo.x * newScale,
    y: pointer.y - mousePointTo.y * newScale,
    scale: newScale
  });
};
</script>

<style scoped>
.miro-board {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
}

.floating-toolbar {
  display: flex;
  gap: 8px;
  padding: 8px;
  background-color: white;
  border-radius: 24px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  pointer-events: auto;
  transform: translateX(-50%);
  transition: top 0.1s ease-out, left 0.1s ease-out;
}

.connector-toolbar {
  padding: 6px 12px;
  border: 1px solid #007bff;
}

.mini-color-swatch {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: transform 0.1s;
}

.mini-color-swatch:hover {
  transform: scale(1.2);
}

.connector-toolbar button {
  padding: 4px 10px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.connector-toolbar button:hover {
  background: #f0f7ff;
}

.divider {
  width: 1px;
  height: 20px;
  background: #eee;
}
</style>
