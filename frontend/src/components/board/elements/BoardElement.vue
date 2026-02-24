<template>
      <v-group
        ref="elementGroupRef"
        :config="elementConfig"
        :id="element.id"    @dragend="handleDragEnd"
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
  </v-group>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import {
  BoardElement,
  ElementType,
  StickyNoteElement,
  TextElement,
} from '@/interfaces/elements';
import StickyNote from './StickyNote.vue';
import BoardText from './BoardText.vue';
import { Group } from 'konva/lib/Group';

const props = defineProps<{
  element: BoardElement;
}>();

const boardStore = useBoardStore();
const elementGroupRef = ref<Group | null>(null);

const isThisElementBeingEdited = computed(() => boardStore.editingElementId === props.element.id);

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

const handleDragEnd = (e: any) => {
  // Update element's position in store after drag
  boardStore.updateElement(props.element.id, {
    x: e.target.x(),
    y: e.target.y(),
  });
};

// No transformend handler here, as transformer is no longer in this component
</script>