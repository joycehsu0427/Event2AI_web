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
      <v-layer>
        <template v-for="element in boardStore.getElements" :key="element.id">
          <BoardElement
            :element="element"
            @transformend="handleTransformEnd"
            @request-edit="handleRequestEdit"
          />
        </template>
        <v-transformer ref="transformerRef" />
      </v-layer>
    </v-stage>
    <div
      v-if="editingSession"
      class="board-text-editor"
      :style="editorWrapperStyle"
    >
      <textarea
        ref="editorTextareaRef"
        v-model="editingText"
        class="board-text-editor__textarea"
        :style="editorTextareaStyle"
        @blur="handleEditorBlur"
        @keydown="handleEditorKeyDown"
      ></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick, type CSSProperties } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import type { Stage as KonvaStage } from 'konva/lib/Stage';
import type { Node as KonvaNode } from 'konva/lib/Node';
import type { KonvaEventObject } from 'konva/lib/Node';
import type { Transformer } from 'konva/lib/shapes/Transformer';
import BoardElement from './elements/BoardElement.vue';
import type { ElementEditRequest } from './elements/editing';

interface EditingSession extends ElementEditRequest {
  originalText: string;
}

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const containerRef = ref<HTMLDivElement | null>(null);
const stageRef = ref<{ getStage: () => KonvaStage } | null>(null);
const transformerRef = ref<{ getNode: () => Transformer } | null>(null);
const editorTextareaRef = ref<HTMLTextAreaElement | null>(null);
const editingSession = ref<EditingSession | null>(null);
const editingText = ref('');

const stageConfig = reactive({
  width: 0,
  height: 0,
  draggable: false,
  x: computed(() => boardStore.canvasTransform.x),
  y: computed(() => boardStore.canvasTransform.y),
  scaleX: computed(() => boardStore.canvasTransform.scale),
  scaleY: computed(() => boardStore.canvasTransform.scale),
});

watch(() => boardStore.getEditingElementId, (editingElementId) => {
  const stageContainer = stageRef.value?.getStage().container();
  if (stageContainer) {
    const konvaContentDiv = stageContainer.parentElement;
    if (konvaContentDiv) {
      konvaContentDiv.style.zIndex = editingElementId !== null ? '0' : '1';
    }
  }
});

watch(() => boardStore.selectedElementIds, (newSelection) => {
  const transformer = transformerRef.value?.getNode();
  const stage = stageRef.value?.getStage();
  if (!transformer || !stage) return;

  if (newSelection.length > 0) {
    const selectedNodes: KonvaNode[] = [];
    newSelection.forEach((id) => {
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

watch(
  () => editingSession.value?.elementId,
  (elementId) => {
    if (!elementId) {
      return;
    }

    const element = boardStore.getElementById(elementId);
    if (!element) {
      closeEditingSession(false);
    }
  },
);

const editorWrapperStyle = computed<CSSProperties>(() => {
  if (!editingSession.value) return {};

  return {
    position: 'fixed',
    left: `${editingSession.value.viewportRect.left}px`,
    top: `${editingSession.value.viewportRect.top}px`,
    width: `${editingSession.value.viewportRect.width}px`,
    height: `${editingSession.value.viewportRect.height}px`,
    zIndex: 9999,
  };
});

const editorTextareaStyle = computed<CSSProperties>(() => {
  if (!editingSession.value) return {};

  return {
    width: '100%',
    height: '100%',
    fontSize: `${editingSession.value.fontSize}px`,
    fontFamily: editingSession.value.fontFamily,
    color: editingSession.value.textColor,
    background: editingSession.value.backgroundColor,
    border: '1px dashed #ccc',
    padding: '5px',
    margin: '0',
    overflow: 'hidden',
    resize: 'none',
    lineHeight: String(editingSession.value.lineHeight),
    boxSizing: 'border-box',
    outline: 'none',
  };
});

const focusEditor = () => {
  nextTick(() => {
    editorTextareaRef.value?.focus();
    editorTextareaRef.value?.select();
  });
};

const openEditingSession = (request: ElementEditRequest) => {
  editingSession.value = {
    ...request,
    originalText: request.text,
  };
  editingText.value = request.text;
  boardStore.setEditingElement(request.elementId);
  focusEditor();
};

const closeEditingSession = (shouldSave: boolean) => {
  const session = editingSession.value;
  if (!session) {
    boardStore.setEditingElement(null);
    return;
  }

  const nextText = editingText.value;
  if (shouldSave && nextText !== session.originalText) {
    boardStore.updateElement(session.elementId, { text: nextText });
    historyStore.addState();
  }

  editingSession.value = null;
  editingText.value = '';
  boardStore.setEditingElement(null);
};

const handleRequestEdit = (request: ElementEditRequest) => {
  if (editingSession.value?.elementId === request.elementId) {
    return;
  }

  if (editingSession.value) {
    closeEditingSession(true);
  }

  openEditingSession(request);
};

const handleEditorBlur = () => {
  closeEditingSession(true);
};

const handleEditorKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    closeEditingSession(true);
    return;
  }

  if (event.key === 'Escape') {
    event.preventDefault();
    closeEditingSession(false);
  }
};

const handleWindowPointerDown = (event: PointerEvent) => {
  if (!editingSession.value) return;

  const target = event.target as Node | null;
  if (target && editorTextareaRef.value?.contains(target)) {
    return;
  }

  closeEditingSession(true);
};

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
    const currentElement = boardStore.getElementById(elementId);
    const nextWidth = Math.max(5, node.width() * scaleX);
    const nextHeight = Math.max(5, node.height() * scaleY);
    const changed =
      !currentElement ||
      currentElement.x !== node.x() ||
      currentElement.y !== node.y() ||
      currentElement.width !== nextWidth ||
      currentElement.height !== nextHeight ||
      (currentElement.rotation || 0) !== rotation;

    boardStore.updateElement(elementId, {
      x: node.x(),
      y: node.y(),
      width: nextWidth,
      height: nextHeight,
      rotation: rotation,
    });

    if (changed) {
      historyStore.addState();
    }
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
  window.addEventListener('pointerdown', handleWindowPointerDown, true);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateStageSize);
  window.removeEventListener('pointerdown', handleWindowPointerDown, true);
  closeEditingSession(false);
});

const isPanning = ref(false);
const lastPointerPosition = reactive({ x: 0, y: 0 });

const handleStageMouseDown = (e: KonvaEventObject<MouseEvent>) => {
  if (boardStore.getEditingElementId) return;

  if (e.target === e.target.getStage()) {
    if (e.evt.button === 2) {
      isPanning.value = true;
      lastPointerPosition.x = e.evt.clientX;
      lastPointerPosition.y = e.evt.clientY;
    } else if (e.evt.button === 0) {
      boardStore.selectElement(null);
    }
  }
};

const handleStageMouseUp = () => {
  isPanning.value = false;
};

const handleStageMouseMove = (e: KonvaEventObject<MouseEvent>) => {
  if (boardStore.getEditingElementId) return;
  if (!isPanning.value) return;

  const dx = e.evt.clientX - lastPointerPosition.x;
  const dy = e.evt.clientY - lastPointerPosition.y;

  const newX = boardStore.canvasTransform.x + dx;
  const newY = boardStore.canvasTransform.y + dy;

  boardStore.setCanvasTransform({ x: newX, y: newY });

  lastPointerPosition.x = e.evt.clientX;
  lastPointerPosition.y = e.evt.clientY;
};

const ZOOM_FACTOR = 1.1;

const handleWheel = (e: KonvaEventObject<WheelEvent>) => {
  e.evt.preventDefault();
  if (boardStore.getEditingElementId) return;

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

.board-text-editor {
  pointer-events: auto;
}

.board-text-editor__textarea {
  display: block;
}
</style>
