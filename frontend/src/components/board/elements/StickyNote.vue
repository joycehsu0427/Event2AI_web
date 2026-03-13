<template>
  <v-rect :config="rectConfig" @dblclick="handleDblClick" @dbltap="handleDblClick" />
  <v-text
    :config="textConfig"
    ref="vTextRef"
    @dblclick="handleDblClick"
    @dbltap="handleDblClick"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElementType, type StickyNoteElement } from '@/interfaces/elements';
import { useBoardStore } from '@/stores/boardStore';
import type { Text } from 'konva/lib/shapes/Text';
import type { ElementEditRequest } from './editing';

const props = defineProps<{
  element: StickyNoteElement;
}>();

const emit = defineEmits<{
  (event: 'request-edit', payload: ElementEditRequest): void;
}>();

const boardStore = useBoardStore();
const vTextRef = ref<{ getNode: () => Text } | null>(null);

const isBeingEdited = computed(() => boardStore.editingElementId === props.element.id);
const textPadding = computed(() =>
  Math.round(Math.min(Math.max(Math.min(props.element.width, props.element.height) * 0.045, 8), 12)),
);
const autoFontSize = computed(() => {
  const innerMinDimension = Math.max(
    Math.min(props.element.width - textPadding.value * 2, props.element.height - textPadding.value * 2),
    40,
  );
  return Math.round(Math.min(Math.max(innerMinDimension * 0.19, 22), 36));
});

const rectConfig = computed(() => ({
  width: props.element.width,
  height: props.element.height,
  fill: props.element.backgroundColor,
  stroke: props.element.stroke || '#333',
  strokeWidth: props.element.strokeWidth || 1,
  cornerRadius: 5,
}));

const textConfig = computed(() => ({
  x: textPadding.value,
  y: textPadding.value,
  width: props.element.width - textPadding.value * 2,
  height: props.element.height - textPadding.value * 2,
  visible: !isBeingEdited.value,
  text: props.element.text,
  fontSize: autoFontSize.value,
  fontFamily: 'Arial',
  fill: props.element.textColor,
  align: 'left',
  verticalAlign: 'top',
  padding: 0,
  lineHeight: 1.05,
}));

const handleDblClick = (e: any) => {
  e.cancelBubble = true;
  e.evt.stopPropagation();

  const textNode = vTextRef.value?.getNode();
  const stage = textNode?.getStage();
  if (!textNode || !stage) {
    return;
  }

  const scaleX = stage.scaleX();
  const scaleY = stage.scaleY();
  const stageRect = stage.container().getBoundingClientRect();
  const textRect = textNode.getClientRect({ skipTransform: false });

  emit('request-edit', {
    elementId: props.element.id,
    elementType: ElementType.StickyNote,
    text: props.element.text,
    viewportRect: {
      left: stageRect.left + textRect.x * scaleX,
      top: stageRect.top + textRect.y * scaleY,
      width: Math.max(textRect.width * scaleX, 40),
      height: Math.max(textRect.height * scaleY, autoFontSize.value * scaleY + 16),
    },
    fontSize: autoFontSize.value * scaleX,
    fontFamily: 'Arial',
    textColor: props.element.textColor,
    backgroundColor: props.element.backgroundColor,
    lineHeight: textNode.lineHeight(),
  });
};
</script>

<style scoped>
</style>
