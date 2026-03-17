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
        v-model="editedText"
        :style="textareaStyle"
        @blur="handleTextareaBlur"
        @keydown.enter="handleEnterKey"
        @keydown.esc="handleEscapeKey"
      ></textarea>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, nextTick, reactive } from 'vue';
import type { StickyNoteElement } from '@/interfaces/elements';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';

const props = defineProps<{
  element: StickyNoteElement;
}>();

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const vTextRef = ref<InstanceType<any> | null>(null);
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const isEditing = ref(false);
const editedText = ref(props.element.text);

// Stores the computed position/size when entering edit mode
const editingPos = reactive({ left: '0px', top: '0px', width: '0px', height: '0px', fontSize: '14px' });

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

let initialElementText = '';

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

  const scale = stage.scaleX();
  const stageRect = stage.container().getBoundingClientRect();
  const textRect = textNode.getClientRect({ skipTransform: false });

  editingPos.left = `${stageRect.left + textRect.x}px`;
  editingPos.top = `${stageRect.top + textRect.y}px`;
  editingPos.width = `${textRect.width}px`;
  editingPos.height = `${textRect.height}px`;
  editingPos.fontSize = `${props.element.fontSize * scale}px`;

  initialElementText = props.element.text;
  isEditing.value = true;
  editedText.value = props.element.text;
  boardStore.setEditingElement(props.element.id);

  nextTick(() => {
    textareaRef.value?.focus();
    textareaRef.value?.select();
  });
};

const handleTextareaBlur = () => {
  if (isEditing.value) {
    isEditing.value = false;
    boardStore.setEditingElement(null);
    if (editedText.value !== initialElementText) {
      boardStore.updateElement(props.element.id, { text: editedText.value });
      historyStore.addState();
    }
  }
};

const handleEnterKey = (e: KeyboardEvent) => {
  if (!e.shiftKey) {
    e.preventDefault();
    textareaRef.value?.blur();
  }
};

const handleEscapeKey = () => {
  isEditing.value = false;
  editedText.value = initialElementText;
  boardStore.setEditingElement(null);
};

const textareaWrapperStyle = computed(() => ({
  position: 'fixed' as const,
  left: editingPos.left,
  top: editingPos.top,
  width: editingPos.width,
  height: editingPos.height,
  zIndex: '9999',
}));

const textareaStyle = computed(() => ({
  width: '100%',
  height: '100%',
  fontSize: editingPos.fontSize,
  fontFamily: 'inherit',
  color: props.element.textColor,
  background: props.element.backgroundColor,
  border: '1px dashed #ccc',
  padding: '5px',
  margin: '0',
  overflow: 'hidden',
  resize: 'none' as const,
  boxSizing: 'border-box' as const,
  outline: 'none',
}));
</script>

<style scoped>
/* Scoped styles if any, but most styling is inline for dynamic positioning */
</style>
