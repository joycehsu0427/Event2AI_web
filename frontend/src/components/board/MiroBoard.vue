<template>
  <div ref="containerRef" class="miro-board">
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
        <template v-for="element in boardStore.getElements" :key="element.id">
          <BoardElement :element="element" @transformend="handleTransformEnd" />
        </template>
        <!-- Konva Transformer for selected elements -->
        <v-transformer ref="transformerRef" />
      </v-layer>
      <!-- A separate layer for selection/transformers to be on top -->
      <!-- <v-layer ref="selectionLayerRef"></v-layer> -->
    </v-stage>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import type { Stage as KonvaStage } from 'konva/lib/Stage';
import type { Layer as KonvaLayer } from 'konva/lib/Layer';
import type { Node as KonvaNode } from 'konva/lib/Node';
import type { KonvaMouseEvent } from 'konva/lib/Node';
import type { Transformer } from 'konva/lib/shapes/Transformer';
import BoardElement from './elements/BoardElement.vue';

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const containerRef = ref<HTMLDivElement | null>(null);
const stageRef = ref<{ getStage: () => KonvaStage } | null>(null);
const elementsLayerRef = ref<{ getStage: () => KonvaLayer } | null>(null);
const transformerRef = ref<Transformer | null>(null); // Ref for Konva Transformer

const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: false, // Pan handled manually
  x: computed(() => boardStore.canvasTransform.x),
  y: computed(() => boardStore.canvasTransform.y),
  scaleX: computed(() => boardStore.canvasTransform.scale),
  scaleY: computed(() => boardStore.canvasTransform.scale),
});

// Watch for changes in board elements to record history
watch(() => boardStore.elements, () => {
  historyStore.addState();
}, { deep: true });

// --- Adjust Konva container Z-index when HTML editing is active ---
watch(() => boardStore.getEditingElementId, (editingElementId) => {
  const stageContainer = stageRef.value?.getStage().container();
  if (stageContainer) {
    // The actual canvas is inside a div with class 'konvajs-content'
    const konvaContentDiv = stageContainer.parentElement;
    if (konvaContentDiv) {
      konvaContentDiv.style.zIndex = editingElementId !== null ? '0' : '1'; // Push Konva behind HTML element
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
      // Find the Konva group node for the selected element
      // Using .find() with a selector like '#id' is more reliable
      const node = stage.find(`#${id}`)[0]; // Assuming BoardElement's root v-group has id
      if (node) {
        selectedNodes.push(node);
      }
    });
    transformer.nodes(selectedNodes);
  } else {
    transformer.nodes([]); // Deselect all
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


// --- Canvas Resizing ---
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

// --- Pan functionality ---
const isPanning = ref(false); // Flag to track if right-click panning is active
const lastPointerPosition = reactive({ x: 0, y: 0 });

const handleStageMouseDown = (e: KonvaMouseEvent) => {
  // If editing an HTML element, don't interact with the canvas
  if (boardStore.getEditingElementId) return;

  // Check if clicking on the stage itself, not an element
  if (e.target === e.target.getStage()) {
    // Right-click for pan
    if (e.evt.button === 2) { // Right mouse button
      isPanning.value = true;
      lastPointerPosition.x = e.evt.clientX;
      lastPointerPosition.y = e.evt.clientY;
    }
    // Left-click for marquee selection (to be implemented)
    else if (e.evt.button === 0) { // Left mouse button
      boardStore.selectElement(null); // Deselect elements when clicking on empty canvas with left-click
      // Start marquee selection logic here
    }
  } else {
    // If clicking on an element, prevent pan (for now)
    // isPanning.value = false; // Ensure pan is not active if clicking an element
    // Do nothing specific here, let element handle its own click
  }
};

const handleStageMouseUp = () => {
  isPanning.value = false;
  // End marquee selection logic here if it was active
};

const handleStageMouseMove = (e: KonvaMouseEvent) => {
  if (boardStore.getEditingElementId) return; // Don't move canvas while editing HTML
  if (!isPanning.value) return; // Only pan if isPanning flag is true

  const dx = e.evt.clientX - lastPointerPosition.x;
  const dy = e.evt.clientY - lastPointerPosition.y;

  const newX = boardStore.canvasTransform.x + dx;
  const newY = boardStore.canvasTransform.y + dy;

  boardStore.setCanvasTransform({ x: newX, y: newY });

  lastPointerPosition.x = e.evt.clientX;
  lastPointerPosition.y = e.evt.clientY;
};

// --- Zoom functionality ---
const ZOOM_FACTOR = 1.1;

const handleWheel = (e: KonvaMouseEvent) => {
  e.evt.preventDefault(); // Prevent page scrolling

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

  const newX = pointer.x - mousePointTo.x * newScale;
  const newY = pointer.y - mousePointTo.y * newScale;

  boardStore.setCanvasTransform({ x: newX, y: newY, scale: newScale });
};
</script>

<style scoped>
.miro-board {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
}
</style>