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
  ConnectorTargetType,
  ConnectorAnchorSide,
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
  childStartPositions: Map<string, { x: number; y: number }>;
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
const elementConfig = computed(() => {
  // 排除 Connector 類型，因為它沒有 x, y, width, height
  if (props.element.type === ElementType.Connector) {
    return {};
  }
  
  return {
    x: props.element.x,
    y: props.element.y,
    width: props.element.width,
    height: props.element.height,
    rotation: props.element.rotation || 0,
    draggable: !isThisElementBeingEdited.value && !boardStore.isDrawingConnector,
    name: 'board-element-group',
    type: props.element.type,
  };
});

const findOverlappingFrameId = (x: number, y: number): string | null => {
  // Avoid accessing width/height for Connector elements
  if (props.element.type === ElementType.Connector) {
    return null;
  }

  const frames = boardStore.getElements.filter(
    (el) => el.type === ElementType.Frame
  ) as FrameElement[];

  // Check if this sticky note's bounds overlap with any frame
  for (const frame of frames) {
    // Check if sticky note is within frame bounds
    if (
      x >= frame.x &&
      x + props.element.width <= frame.x + frame.width &&
      y >= frame.y &&
      y + props.element.height <= frame.y + frame.height
    ) {
      return frame.id;
    }
  }
  return null;
};

const handleClick = (e: any) => {
  e.cancelBubble = true;

  // If in drawing connector mode, handle connector logic
  if (boardStore.isDrawingConnector) {
    handleConnectorModeClick(e);
    return;
  }

  // Normal select logic
  boardStore.selectElement(props.element.id, e.evt.shiftKey || e.evt.ctrlKey || e.evt.metaKey);
};

const handleConnectorModeClick = (e: any) => {
  const stage = e.target.getStage();
  const pointer = stage.getPointerPosition();
  if (!pointer) return;

  const targetType = mapElementTypeToConnectorTargetType(props.element.type);
  
  if (!boardStore.connectorDrawingStart?.targetId) {
    // Start drawing from this element
    boardStore.startDrawingConnector({
      targetId: props.element.id,
      targetType: targetType,
      // Default to auto-side or closest side later, now just start
    });
  } else {
    // Complete drawing to this element
    // This part is actually handled better in MiroBoard.vue or via a global event
    // because we need to know where the user clicked exactly on the object.
  }
};

const mapElementTypeToConnectorTargetType = (type: ElementType): ConnectorTargetType => {
  switch(type) {
    case ElementType.StickyNote: return ConnectorTargetType.STICKY_NOTE;
    case ElementType.Text: return ConnectorTargetType.TEXT_BOX;
    case ElementType.Frame: return ConnectorTargetType.FRAME;
    case ElementType.DomainModelItem: return ConnectorTargetType.DOMAIN_MODEL_ITEM;
    default: return ConnectorTargetType.FREE_POINT;
  }
};

const handleDragStart = (e: any) => {
  if (props.element.type !== ElementType.Frame) {
    return;
  }

  const childStartPositions = new Map<string, { x: number; y: number }>();
  boardStore.getElements.forEach((el) => {
    if (el.id !== props.element.id && el.frameId === props.element.id) {
      childStartPositions.set(el.id, { x: el.x, y: el.y });
    }
  });

  frameDragState.value = {
    frameStartX: e.target.x(),
    frameStartY: e.target.y(),
    childStartPositions,
  };
};

const handleDragMove = (e: any) => {
  const newX = e.target.x();
  const newY = e.target.y();

  if (props.element.type === ElementType.Frame && frameDragState.value) {
    const stage = e.target.getStage();
    if (stage) {
      const dx = newX - frameDragState.value.frameStartX;
      const dy = newY - frameDragState.value.frameStartY;

      frameDragState.value.childStartPositions.forEach((startPos, childId) => {
        const childNode = stage.find(`#${childId}`)[0] as KonvaNode | undefined;
        if (childNode) {
          const cx = startPos.x + dx;
          const cy = startPos.y + dy;
          childNode.x(cx);
          childNode.y(cy);
          // Update child store position for connectors attached to children
          boardStore.updateElementLocal(childId, { x: cx, y: cy });
        }
      });
    }
  }

  // Update this element's position in store for connectors to follow
  boardStore.updateElementLocal(props.element.id, {
    x: newX,
    y: newY,
  });

  e.target.getLayer()?.batchDraw();
};

const handleDragEnd = (e: any) => {
  const newX = e.target.x();
  const newY = e.target.y();

  if (props.element.type === ElementType.Frame && frameDragState.value) {
    const dx = newX - frameDragState.value.frameStartX;
    const dy = newY - frameDragState.value.frameStartY;

    frameDragState.value.childStartPositions.forEach((startPos, childId) => {
      boardStore.updateElement(childId, {
        x: startPos.x + dx,
        y: startPos.y + dy,
      });
    });

    frameDragState.value = null;
  }

  if (props.element.type === ElementType.StickyNote || 
      props.element.type === ElementType.Text || 
      props.element.type === ElementType.DomainModelItem) {
    // Check if element is now within a different frame after drag
    const newFrameId = findOverlappingFrameId(newX, newY);
    const currentFrameId = props.element.frameId || null;

    // Only update if frameId has changed
    if (newFrameId !== currentFrameId) {
      boardStore.updateElement(props.element.id, {
        frameId: newFrameId,
      });
    }
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
