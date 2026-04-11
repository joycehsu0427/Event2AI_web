<template>
  <v-rect :config="rectConfig" />
  <v-text
    :config="textConfig"
    :visible="!isEditing"
    ref="vTextRef"
    @dblclick="handleDblClick"
    @dbltap="handleDblClick"
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
import { computed, ref, nextTick } from 'vue';
import type { TextElement } from '@/types/elements';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import { useBoardElementEditor } from './boardElementContext';

const props = defineProps<{
  element: TextElement;
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
  fill: 'transparent',
  // Background rect for stable transformer handles and hit detection
}));

const textConfig = computed(() => ({
  x: 0,
  y: 0,
  width: props.element.width,
  height: props.element.height,
  text: props.element.text,
  fontSize: props.element.fontSize,
  fontFamily: props.element.fontFamily,
  fill: props.element.textColor,
  align: 'left',
  verticalAlign: 'top',
}));

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