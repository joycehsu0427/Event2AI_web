<template>
  <div ref="containerRef" class="miro-board" @contextmenu.prevent>
    <v-stage
      ref="stageRef"
      :config="stageConfig"
      @wheel="handleWheel"
      @mousedown="handleStageMouseDown"
      @mouseup="handleStageMouseUp"
      @mousemove="handleStageMouseMove"
      @contextmenu.prevent="() => {}"
    >
      <v-layer ref="elementsLayerRef">
        <!-- Elements will be rendered here -->
        <template v-for="element in renderedElements" :key="element.id">
          <BoardElement :element="element" @transformend="handleTransformEnd" />
        </template>
        <!-- Konva Transformer for selected elements -->
        <v-transformer ref="transformerRef" />

        <!-- Marquee Selection Rectangle -->
        <v-rect
          v-if="isSelecting"
          :config="selectionRectConfig"
        />
      </v-layer>
    </v-stage>

    <!-- Floating Mini-Toolbar for selected Sticky Note -->
    <div v-if="showFloatingToolbar" class="floating-toolbar" :style="floatingToolbarStyle">
      <div v-for="color in stickyNoteColors" :key="color"
           class="mini-color-swatch"
           :style="{ backgroundColor: color }"
           @click="boardStore.updateSelectedElementsColor(color)"
           :title="getNameByHex(color)"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import type { Stage as KonvaStage } from 'konva/lib/Stage';
import type { Layer as KonvaLayer } from 'konva/lib/Layer';
import type { Node as KonvaNode } from 'konva/lib/Node';
import type { KonvaMouseEvent } from 'konva/lib/Node';
import type { Transformer } from 'konva/lib/shapes/Transformer';
import BoardElement from './elements/BoardElement.vue';
import { ElementType } from '@/types/elements';
import { STICKY_NOTE_COLOR_PALETTE, getNameByHex } from '@/constants/colors';

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const containerRef = ref<HTMLDivElement | null>(null);
const stageRef = ref<{ getStage: () => KonvaStage } | null>(null);
const elementsLayerRef = ref<{ getStage: () => KonvaLayer } | null>(null);
const transformerRef = ref<Transformer | null>(null); // Ref for Konva Transformer

const stickyNoteColors = STICKY_NOTE_COLOR_PALETTE;

const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: false, // Pan handled manually
  x: computed(() => boardStore.canvasTransform.x),
  y: computed(() => boardStore.canvasTransform.y),
  scaleX: computed(() => boardStore.canvasTransform.scale),
  scaleY: computed(() => boardStore.canvasTransform.scale),
});

const renderedElements = computed(() => {
  const layerPriority = {
    [ElementType.Frame]: 0,
    [ElementType.Text]: 1,
    [ElementType.StickyNote]: 2,
  } as const;

  return [...boardStore.getElements].sort(
    (a, b) => layerPriority[a.type] - layerPriority[b.type]
  );
});

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
  
  // Convert stage coordinates to screen coordinates
  // Position it at the horizontal center of the sticky note
  const left = (element.x * scale) + x + (element.width * scale / 2);
  const top = (element.y * scale) + y - 100; // 60px above the element

  return {
    position: 'absolute' as const,
    left: `${left}px`,
    top: `${top}px`,
    zIndex: 1002,
  };
});

// Watch for changes in board elements to record history
watch(() => boardStore.elements, () => {
  historyStore.addState();
}, { deep: true });

// --- Adjust Konva container Z-index when HTML editing is active ---
watch(() => boardStore.getEditingElementId, (editingElementId) => {
  const stageContainer = stageRef.value?.getStage().container();
  if (stageContainer) {
    const konvaContentDiv = stageContainer.parentElement;
    if (konvaContentDiv) {
      konvaContentDiv.style.zIndex = editingElementId !== null ? '0' : '1';
    }
  }
});

// --- Transformer Logic ---
watch(() => boardStore.selectedElementIds, (newSelection) => {
  const transformer = transformerRef.value?.getNode();
  const stage = stageRef.value?.getStage();
  if (!transformer || !stage) return;

  if (newSelection.length > 0) {
    const selectedNodes: KonvaNode[] = [];
    newSelection.forEach(id => {
      const node = stage.find(`#${id}`)[0];
      if (node) {
        selectedNodes.push(node);
      }
    });
    transformer.nodes(selectedNodes);
  } else {
    transformer.nodes([]);
  }
  transformer.getLayer()?.batchDraw();
}, { immediate: true });

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

// --- Pan & Selection functionality ---
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

  if (e.target === stage) {
    if (e.evt.button === 2) {
      e.evt.preventDefault();
      isPanning.value = true;
      lastPointerPosition.x = e.evt.clientX;
      lastPointerPosition.y = e.evt.clientY;
    }
    else if (e.evt.button === 0) {
      boardStore.selectElement(null);
      const pointer = stage.getPointerPosition();
      if (pointer) {
        isSelecting.value = true;
        const stageX = (pointer.x - stage.x()) / stage.scaleX();
        const stageY = (pointer.y - stage.y()) / stage.scaleY();
        selectionStart.x = stageX;
        selectionStart.y = stageY;
        selectionRectConfig.x = stageX;
        selectionRectConfig.y = stageY;
        selectionRectConfig.width = 0;
        selectionRectConfig.height = 0;
      }
    }
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
    const pointer = stage.getPointerPosition();
    if (!pointer) return;
    const stageX = (pointer.x - stage.x()) / stage.scaleX();
    const stageY = (pointer.y - stage.y()) / stage.scaleY();

    selectionRectConfig.x = Math.min(stageX, selectionStart.x);
    selectionRectConfig.y = Math.min(stageY, selectionStart.y);
    selectionRectConfig.width = Math.abs(stageX - selectionStart.x);
    selectionRectConfig.height = Math.abs(stageY - selectionStart.y);

    const selectedIds: string[] = [];
    boardStore.getElements.forEach((el) => {
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
  padding: 6px;
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  pointer-events: auto;
  transform: translateX(-50%);
  transition: top 0.1s ease-out, left 0.1s ease-out;
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
</style>