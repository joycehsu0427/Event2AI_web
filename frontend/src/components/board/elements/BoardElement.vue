<template>
  <v-group
    ref="elementGroupRef"
    :config="elementConfig"
    :id="element.id"
    @dragstart="handleDragStart"
    @dragmove="handleDragMove"
    @dragend="handleDragEnd"
    @click="handleClick"
    @tap="handleClick"
  >
    <!-- Render specific element type based on element.type -->
    <template v-if="element.type === ElementType.StickyNote">
      <StickyNote :element="(element as StickyNoteElement)" />
    </template>
    <template v-else-if="element.type === ElementType.Text">
      <BoardText :element="(element as TextElement)" />
    </template>
    <template v-else-if="element.type === ElementType.Frame">
      <Frame :element="(element as FrameElement)" />
    </template>
    <template v-else-if="element.type === ElementType.DomainModelItem">
      <DomainModelItem :element="(element as DomainModelItemElement)" />
    </template>
  </v-group>
</template>

<script setup lang="ts">
import { ref, computed, provide, onBeforeUnmount } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import {
  type BoardElement,
  ElementType,
  type FrameElement,
  type StickyNoteElement,
  type TextElement,
  type DomainModelItemElement,
} from '@/types/elements';
import StickyNote from './StickyNote.vue';
import BoardText from './BoardText.vue';
import Frame from './Frame.vue';
import DomainModelItem from './DomainModelItem.vue';
import { Group } from 'konva/lib/Group';
import type { Node as KonvaNode } from 'konva/lib/Node';
import { boardElementEditorKey } from './boardElementContext';
import { useInlineEditorController } from './useInlineEditorController';

const props = defineProps<{
  element: BoardElement;
}>();

const boardStore = useBoardStore();
const elementGroupRef = ref<Group | null>(null);

const frameDragState = ref<{
  frameStartX: number;
  frameStartY: number;
  stickyStartPositions: Map<string, { x: number; y: number }>;
} | null>(null);

const editor = useInlineEditorController({
  elementId: props.element.id,
  styles: {
    textarea: (position) => ({
      width: '100%',
      height: '100%',
      fontSize: position.fontSize,
      fontFamily: 'inherit',
      color: props.element.type === ElementType.StickyNote ? props.element.textColor : '#2c3e50',
      background: props.element.type === ElementType.StickyNote ? props.element.backgroundColor : 'transparent',
      border: '1px dashed #ccc',
      padding: '5px',
      margin: '0',
      overflow: 'hidden',
      resize: 'none' as const,
      boxSizing: 'border-box' as const,
      outline: 'none',
    }),
    input: (position) => ({
      width: '100%',
      height: '100%',
      fontSize: position.fontSize,
      fontWeight: '700',
      color: '#2c3e50',
      background: 'rgba(255, 255, 255, 0.92)',
      border: '1px dashed #7f8c8d',
      borderRadius: '4px',
      padding: '2px 6px',
      margin: '0',
      boxSizing: 'border-box' as const,
      outline: 'none',
    }),
  },
});

const isThisElementBeingEdited = computed(() => boardStore.editingElementId === props.element.id);

provide(boardElementEditorKey, editor);

// Configuration for the Konva Group that wraps each element
const elementConfig = computed(() => ({
  x: props.element.x,
  y: props.element.y,
  width: props.element.width,
  height: props.element.height,
  rotation: props.element.rotation || 0,
  draggable: !isThisElementBeingEdited.value, // Make element non-draggable if it's being edited
}));

const handleClick = (e: any) => {
  // Prevent event bubbling to the stage if an element is clicked
  e.cancelBubble = true;
  // Select this element
  boardStore.selectElement(props.element.id, e.evt.shiftKey || e.evt.ctrlKey || e.evt.metaKey);
};

const handleDragStart = (e: any) => {
  if (props.element.type !== ElementType.Frame) {
    return;
  }

  const stickyStartPositions = new Map<string, { x: number; y: number }>();
  boardStore.getElements.forEach((el) => {
    if (el.type === ElementType.StickyNote && el.frameId === props.element.id) {
      stickyStartPositions.set(el.id, { x: el.x, y: el.y });
    }
  });

  frameDragState.value = {
    frameStartX: e.target.x(),
    frameStartY: e.target.y(),
    stickyStartPositions,
  };
};

const handleDragMove = (e: any) => {
  if (props.element.type !== ElementType.Frame || !frameDragState.value) {
    return;
  }

  const stage = e.target.getStage();
  if (!stage) {
    return;
  }

  const dx = e.target.x() - frameDragState.value.frameStartX;
  const dy = e.target.y() - frameDragState.value.frameStartY;

  frameDragState.value.stickyStartPositions.forEach((startPos, stickyId) => {
    const stickyNode = stage.find(`#${stickyId}`)[0] as KonvaNode | undefined;
    if (!stickyNode) {
      return;
    }

    stickyNode.x(startPos.x + dx);
    stickyNode.y(startPos.y + dy);
  });

  e.target.getLayer()?.batchDraw();
};

const handleDragEnd = (e: any) => {
  const newX = e.target.x();
  const newY = e.target.y();

  if (props.element.type === ElementType.Frame && frameDragState.value) {
    const dx = newX - frameDragState.value.frameStartX;
    const dy = newY - frameDragState.value.frameStartY;

    frameDragState.value.stickyStartPositions.forEach((startPos, stickyId) => {
      boardStore.updateElement(stickyId, {
        x: startPos.x + dx,
        y: startPos.y + dy,
      });
    });

    frameDragState.value = null;
  }

  // Update element's position in store after drag
  boardStore.updateElement(props.element.id, {
    x: newX,
    y: newY,
  });
};

onBeforeUnmount(() => {
  boardStore.setEditingElement(null);
  frameDragState.value = null;
});

</script>
