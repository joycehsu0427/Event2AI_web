<template>
  <v-group
    :config="elementConfig"
    :id="element.id"
    @dragend="handleDragEnd"
    @click="handleClick"
    @tap="handleClick"
  >
    <template v-if="element.type === ElementType.StickyNote">
      <StickyNote :element="(element as StickyNoteElement)" @request-edit="handleRequestEdit" />
    </template>
    <template v-else-if="element.type === ElementType.Text">
      <BoardText :element="(element as TextElement)" @request-edit="handleRequestEdit" />
    </template>
  </v-group>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import {
  type BoardElement,
  ElementType,
  type StickyNoteElement,
  type TextElement,
} from '@/interfaces/elements';
import StickyNote from './StickyNote.vue';
import BoardText from './BoardText.vue';
import type { ElementEditRequest } from './editing';

const props = defineProps<{
  element: BoardElement;
}>();

const emit = defineEmits<{
  (event: 'request-edit', payload: ElementEditRequest): void;
}>();

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const isThisElementBeingEdited = computed(() => boardStore.editingElementId === props.element.id);

const elementConfig = computed(() => ({
  x: props.element.x,
  y: props.element.y,
  width: props.element.width,
  height: props.element.height,
  rotation: props.element.rotation || 0,
  draggable: !isThisElementBeingEdited.value,
}));

const handleClick = (e: any) => {
  e.cancelBubble = true;
  boardStore.selectElement(props.element.id, e.evt.shiftKey || e.evt.ctrlKey || e.evt.metaKey);
};

const handleDragEnd = (e: any) => {
  const nextX = e.target.x();
  const nextY = e.target.y();
  const positionChanged = nextX !== props.element.x || nextY !== props.element.y;

  boardStore.updateElement(props.element.id, {
    x: nextX,
    y: nextY,
  });

  if (positionChanged) {
    historyStore.addState();
  }
};

const handleRequestEdit = (payload: ElementEditRequest) => {
  emit('request-edit', payload);
};
</script>
