<template>
  <v-rect :config="rectConfig" @dblclick="handleDblClick" @dbltap="handleDblClick" />
  <v-text
    :config="textConfig"
    ref="vTextRef"
    @dblclick="handleDblClick"
    @dbltap="handleDblClick"
    v-show="!isEditing"
  />
  <Teleport to="body">
    <div v-if="isEditing" :style="textareaWrapperStyle">
      <textarea
        ref="textareaRef"
        v-model="draftValue"
        :style="textareaStyle"
        @blur="handleTextareaBlur"
        @keydown.enter="handleEnterKey"
        @keydown.esc="handleEscapeKey"
      ></textarea>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, nextTick, watch } from 'vue';
import type { StickyNoteElement, FrameElement } from '@/types/elements';
import { ElementType } from '@/types/elements';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import { useBoardElementEditor } from './boardElementContext';

const props = defineProps<{
  element: StickyNoteElement;
}>();

const boardStore = useBoardStore();
const historyStore = useHistoryStore();
const editor = useBoardElementEditor();
const {
  isEditing,
  draftValue,
  textareaWrapperStyle,
  textareaStyle,
  startEditing,
  commitEditing,
  cancelEditing,
  handleEnterKey: handleEditorEnterKey,
} = editor;

const vTextRef = ref<InstanceType<any> | null>(null);
const textareaRef = ref<HTMLTextAreaElement | null>(null);

const rectConfig = computed(() => ({
  width: props.element.width,
  height: props.element.height,
  fill: props.element.backgroundColor,
  stroke: props.element.stroke || '#333',
  strokeWidth: props.element.strokeWidth || 1,
  cornerRadius: 5,
}));

const textConfig = computed(() => ({
  x: 5,
  y: 5,
  width: props.element.width - 10,
  height: props.element.height - 10,
  text: props.element.text,
  fontSize: props.element.fontSize,
  fill: props.element.textColor,
  align: 'left',
  verticalAlign: 'top',
  padding: 5,
}));

// Helper function to check if sticky note overlaps with any frame
const findOverlappingFrameId = (): string | null => {
  const frames = boardStore.getElements.filter(
    (el) => el.type === ElementType.Frame
  ) as FrameElement[];

  // Check if this sticky note's bounds overlap with any frame
  for (const frame of frames) {
    // Check if sticky note is within frame bounds
    if (
      props.element.x >= frame.x &&
      props.element.x + props.element.width <= frame.x + frame.width &&
      props.element.y >= frame.y &&
      props.element.y + props.element.height <= frame.y + frame.height
    ) {
      return frame.id;
    }
  }
  return null;
};

// Watch for position changes and update frameId accordingly
watch(
  () => [props.element.x, props.element.y],
  () => {
    const newFrameId = findOverlappingFrameId();
    const currentFrameId = props.element.frameId || null;

    // Only update if frameId has changed
    if (newFrameId !== currentFrameId) {
      boardStore.updateElement(props.element.id, {
        frameId: newFrameId,
      });
    }
  }
);

const handleDblClick = (e: any) => {
  e.evt.stopPropagation();

  if (!vTextRef.value) {
    console.warn('StickyNote: vTextRef is null, cannot edit.');
    return;
  }

  // Calculate position BEFORE setting isEditing so vTextRef is still valid
  const textNode = vTextRef.value.getNode();
  const stage = textNode.getStage();
  if (!stage) return;

  if (!startEditing({
    anchorNode: textNode,
    initialValue: props.element.text,
    fontSize: props.element.fontSize,
    onCommit: (value) => {
      boardStore.updateElement(props.element.id, { text: value });
      historyStore.addState();
    },
  })) {
    return;
  }

  nextTick(() => {
    textareaRef.value?.focus();
    textareaRef.value?.select();
  });
};

const handleTextareaBlur = () => {
  commitEditing();
};

const handleEnterKey = (e: KeyboardEvent) => {
  handleEditorEnterKey(e, true);
};

const handleEscapeKey = () => {
  cancelEditing();
};
</script>

<style scoped>
/* Scoped styles if any, but most styling is inline for dynamic positioning */
</style>
