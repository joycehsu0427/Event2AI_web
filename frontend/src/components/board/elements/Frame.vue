<template>
  <v-group>
    <v-rect :config="rectConfig" />
    <v-text
      ref="vTextRef"
      :config="titleConfig"
      @dblclick="startEditing"
      @dbltap="startEditing"
    />
  </v-group>

  <Teleport to="body">
    <div v-if="isEditing" :style="editorWrapperStyle">
      <input
        ref="titleInputRef"
        v-model="editedTitle"
        :style="editorInputStyle"
        @input="handleLiveInput"
        @blur="finishEditing"
        @keydown.enter.prevent="finishEditing"
        @keydown.esc.prevent="cancelEditing"
      />
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import type { FrameElement } from '@/interfaces/elements';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';

const props = defineProps<{
  element: FrameElement;
}>();

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const vTextRef = ref<InstanceType<any> | null>(null);
const titleInputRef = ref<HTMLInputElement | null>(null);
const isEditing = ref(false);
const editedTitle = ref('');
const initialTitle = ref('');
const editorPos = reactive({
  left: '0px',
  top: '0px',
  width: '0px',
  height: '0px',
  fontSize: '18px',
});

const rectConfig = computed(() => ({
  width: props.element.width,
  height: props.element.height,
  fill: props.element.fill ?? '#ffffff',
  stroke: props.element.stroke ?? '#5f6b7a',
  strokeWidth: props.element.strokeWidth ?? 2,
  cornerRadius: 8,
  shadowColor: 'rgba(0, 0, 0, 0.1)',
  shadowBlur: 4,
  shadowOffset: { x: 0, y: 2 },
}));

const titleConfig = computed(() => ({
  x: 0,
  y: -26,
  width: Math.max(props.element.width - 24, 0),
  text: props.element.title,
  fontSize: 18,
  fontStyle: 'bold',
  fontFamily: 'Arial',
  fill: isEditing.value ? 'rgba(31, 41, 55, 0)' : '#1f2937',
  listening: true,
}));

const editorWrapperStyle = computed(() => ({
  position: 'fixed' as const,
  left: editorPos.left,
  top: editorPos.top,
  width: editorPos.width,
  height: editorPos.height,
  zIndex: '9999',
}));

const editorInputStyle = computed(() => ({
  width: '100%',
  height: '100%',
  border: 'none',
  borderBottom: '1px solid #5f6b7a',
  background: 'transparent',
  color: '#1f2937',
  fontSize: editorPos.fontSize,
  fontFamily: 'Arial',
  fontWeight: '700',
  outline: 'none',
  padding: '0',
  margin: '0',
  lineHeight: '1.2',
  boxSizing: 'border-box' as const,
}));

const updateEditorPosition = () => {
  const textNode = vTextRef.value?.getNode?.();
  const stage = textNode?.getStage?.();
  if (!textNode || !stage) return;

  const stageRect = stage.container().getBoundingClientRect();
  const textRect = textNode.getClientRect({ skipTransform: false });
  const scale = stage.scaleX();

  editorPos.left = `${stageRect.left + textRect.x}px`;
  editorPos.top = `${stageRect.top + textRect.y}px`;
  editorPos.width = `${Math.max(textRect.width, 120)}px`;
  editorPos.height = `${Math.max(textRect.height, 28)}px`;
  editorPos.fontSize = `${18 * scale}px`;
};

const startEditing = (e: any) => {
  e.evt.stopPropagation();

  const textNode = vTextRef.value?.getNode?.();
  const stage = textNode?.getStage?.();
  if (!textNode || !stage) return;

  updateEditorPosition();

  initialTitle.value = props.element.title;
  editedTitle.value = props.element.title;
  isEditing.value = true;
  boardStore.setEditingElement(props.element.id);

  nextTick(() => {
    const input = titleInputRef.value;
    if (!input) return;
    input.focus();
    input.setSelectionRange(input.value.length, input.value.length);
  });
};

const handleLiveInput = () => {
  boardStore.updateElement(props.element.id, { title: editedTitle.value });
};

const finishEditing = () => {
  if (!isEditing.value) return;

  const changed = editedTitle.value !== initialTitle.value;
  isEditing.value = false;
  boardStore.setEditingElement(null);

  if (changed) {
    boardStore.updateElement(props.element.id, { title: editedTitle.value });
    historyStore.addState();
  }
};

const cancelEditing = () => {
  if (!isEditing.value) return;

  boardStore.updateElement(props.element.id, { title: initialTitle.value });
  editedTitle.value = initialTitle.value;
  isEditing.value = false;
  boardStore.setEditingElement(null);
};

watch(
  () => [
    isEditing.value,
    boardStore.canvasTransform.x,
    boardStore.canvasTransform.y,
    boardStore.canvasTransform.scale,
    props.element.x,
    props.element.y,
    props.element.width,
    props.element.height,
    props.element.title,
  ],
  () => {
    if (!isEditing.value) return;
    nextTick(() => {
      updateEditorPosition();
    });
  }
);

const handleViewportChange = () => {
  if (!isEditing.value) return;
  updateEditorPosition();
};

onMounted(() => {
  window.addEventListener('resize', handleViewportChange);
  window.addEventListener('scroll', handleViewportChange, true);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleViewportChange);
  window.removeEventListener('scroll', handleViewportChange, true);
});
</script>
