<template>
  <v-text
    :config="textConfig"
    ref="vTextRef"
    @dblclick="handleDblClick"
    @dbltap="handleDblClick"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElementType, type TextElement } from '@/interfaces/elements';
import { useBoardStore } from '@/stores/boardStore';
import type { Text } from 'konva/lib/shapes/Text';
import type { ElementEditRequest } from './editing';

const props = defineProps<{
  element: TextElement;
}>();

const emit = defineEmits<{
  (event: 'request-edit', payload: ElementEditRequest): void;
}>();

const boardStore = useBoardStore();
const vTextRef = ref<{ getNode: () => Text } | null>(null);

const isBeingEdited = computed(() => boardStore.editingElementId === props.element.id);

const textConfig = computed(() => ({
  x: 0,
  y: 0,
  width: props.element.width,
  height: props.element.height,
  visible: !isBeingEdited.value,
  text: props.element.text,
  fontSize: props.element.fontSize,
  fontFamily: props.element.fontFamily,
  fill: props.element.textColor,
  align: 'left',
  verticalAlign: 'top',
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
    elementType: ElementType.Text,
    text: props.element.text,
    viewportRect: {
      left: stageRect.left + textRect.x * scaleX,
      top: stageRect.top + textRect.y * scaleY,
      width: Math.max(textRect.width * scaleX, 40),
      height: Math.max(textRect.height * scaleY, props.element.fontSize * scaleY + 16),
    },
    fontSize: props.element.fontSize * scaleX,
    fontFamily: props.element.fontFamily,
    textColor: props.element.textColor,
    backgroundColor: 'transparent',
    lineHeight: textNode.lineHeight(),
  });
};
</script>

<style scoped>
</style>
