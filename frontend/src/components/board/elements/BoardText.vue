<template>
  <v-text
    :config="textConfig"
    ref="vTextRef"
    @dblclick="handleDblClick"
    @dbltap="handleDblClick"
  />
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
</template>

<script setup lang="ts">
import { computed, ref, nextTick, onMounted, onUnmounted } from 'vue';
import { TextElement } from '@/interfaces/elements';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import type { Text } from 'konva/lib/shapes/Text';
import Konva from 'konva';

const props = defineProps<{
  element: TextElement;
}>();

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const vTextRef = ref<Text | null>(null);
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const isEditing = ref(false);
const editedText = ref(props.element.text);

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
  // Removed listening: false from here, as parent group should handle dragging
}));

let initialElementText = ''; // To store text before editing, for history

const handleDblClick = (e: any) => {
  e.evt.stopPropagation(); // Prevent event from bubbling to parent Konva elements or stage

  if (!vTextRef.value) return;

  initialElementText = props.element.text;

  isEditing.value = true;
  console.log('BoardText: isEditing set to', isEditing.value);
  editedText.value = props.element.text;
  boardStore.setEditingElement(props.element.id); // Notify store that this element is being edited

  nextTick(() => {
    if (textareaRef.value && vTextRef.value) {
      const textNode = vTextRef.value.getNode();
      const stage = textNode.getStage();
      if (!stage) return;

      const scale = stage.scaleX(); // Assuming uniform scale
      const stageRect = stage.container().getBoundingClientRect();
      const textRect = textNode.getClientRect({ skipTransform: false });

      // Calculate the absolute position and size on the screen
      const x = stageRect.left + textRect.x * scale;
      const y = stageRect.top + textRect.y * scale;
      const width = textRect.width * scale;
      const height = textRect.height * scale;

      textareaRef.value.style.position = 'absolute';
      textareaRef.value.style.left = `${x}px`;
      textareaRef.value.style.top = `${y}px`;
      textareaRef.value.style.width = `${width}px`;
      textareaRef.value.style.height = `${height}px`;
      textareaRef.value.style.fontSize = `${props.element.fontSize * scale}px`;
      textareaRef.value.style.fontFamily = props.element.fontFamily;
      textareaRef.value.style.color = props.element.textColor;
      textareaRef.value.style.background = 'transparent';
      textareaRef.value.style.border = '1px dashed #ccc';
      textareaRef.value.style.padding = '5px';
      textareaRef.value.style.margin = '0';
      textareaRef.value.style.overflow = 'hidden';
      textareaRef.value.style.resize = 'none';
      textareaRef.value.style.lineHeight = textNode.lineHeight().toString(); // Match Konva line height
      textareaRef.value.style.boxSizing = 'border-box';
      textareaRef.value.style.outline = 'none'; // Remove focus outline

      setTimeout(() => {
        textareaRef.value?.focus();
        console.log('BoardText: document.activeElement after delayed focus:', document.activeElement);
        setTimeout(() => {
          textareaRef.value?.select();
          console.log('BoardText: document.activeElement after select:', document.activeElement);
        }, 0);
      }, 0);
    }
  });
};

const handleTextareaBlur = () => {
  if (isEditing.value) {
    isEditing.value = false;
    boardStore.setEditingElement(null); // Notify store that editing has ended
    if (editedText.value !== initialElementText) {
      boardStore.updateElement(props.element.id, { text: editedText.value });
      historyStore.addState(); // Record state change
    }
  }
};

const handleEnterKey = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) { // Shift+Enter for new line
    e.preventDefault(); // Prevent new line in textarea
    textareaRef.value?.blur(); // Trigger blur to save changes
  }
};

const handleEscapeKey = () => {
  isEditing.value = false;
  editedText.value = initialElementText; // Revert text if escape is pressed
  boardStore.setEditingElement(null); // Notify store that editing has ended
};

// Styles for the textarea wrapper to contain the absolutely positioned textarea
const textareaWrapperStyle = computed(() => {
  if (!vTextRef.value) return {};
  const textNode = vTextRef.value.getNode();
  const stage = textNode.getStage();
  if (!stage) return {};

  const scale = stage.scaleX();
  const stageRect = stage.container().getBoundingClientRect();
  const textRect = textNode.getClientRect({ skipTransform: false });

  return {
    position: 'fixed', // Use fixed to position relative to viewport
    left: `${stageRect.left + textRect.x * scale}px`,
    top: `${stageRect.top + textRect.y * scale}px`,
    width: `${textRect.width * scale}px`,
    height: `${textRect.height * scale}px`,
    // Ensure it's above the canvas
    zIndex: 9999,
    // pointerEvents: 'none', // Allow clicks to pass through to the textarea - REMOVED
  };
});

const textareaStyle = computed(() => {
  if (!vTextRef.value) return {};
  const textNode = vTextRef.value.getNode();
  const stage = textNode.getStage();
  if (!stage) return {};

  const scale = stage.scaleX();

  return {
    position: 'absolute',
    left: '0',
    top: '0',
    width: '100%',
    height: '100%',
    fontSize: `${props.element.fontSize * scale}px`,
    fontFamily: props.element.fontFamily,
    color: props.element.textColor,
    background: 'transparent',
    border: '1px dashed #ccc',
    padding: '5px',
    margin: '0',
    overflow: 'hidden',
    resize: 'none',
    lineHeight: textNode.lineHeight().toString(),
    boxSizing: 'border-box',
    outline: 'none',
    pointerEvents: 'auto', // Important: Make textarea interactive
  };
});
</script>

<style scoped>
/* Scoped styles if any, but most styling is inline for dynamic positioning */
</style>