<template>
	<v-rect :config="rectConfig" />
	<v-text
		ref="titleTextRef"
		:config="titleTextConfig"
		@dblclick="handleDblClick"
		@dbltap="handleDblClick"
	/>

	<Teleport to="body">
		<div v-if="isEditing" :style="inputWrapperStyle">
			<input
				ref="inputRef"
				v-model="editedTitle"
				type="text"
				:style="inputStyle"
				@input="handleInput"
				@blur="handleInputBlur"
				@keydown.enter="handleEnterKey"
				@keydown.esc="handleEscapeKey"
			/>
		</div>
	</Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue';
import type { FrameElement } from '@/interfaces/elements';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';

const props = defineProps<{
	element: FrameElement;
}>();

const boardStore = useBoardStore();
const historyStore = useHistoryStore();

const titleTextRef = ref<InstanceType<any> | null>(null);
const inputRef = ref<HTMLInputElement | null>(null);
const isEditing = ref(false);
const editedTitle = ref(props.element.title);
const initialTitle = ref(props.element.title);

const editingPos = reactive({
	left: '0px',
	top: '0px',
	width: '200px',
	height: '26px',
	fontSize: '18px',
});

watch(
	() => props.element.title,
	(title) => {
		if (!isEditing.value) {
			editedTitle.value = title;
		}
	}
);

const displayTitle = computed(() => (isEditing.value ? editedTitle.value : props.element.title));

const rectConfig = computed(() => ({
	x: 0,
	y: 0,
	width: props.element.width,
	height: props.element.height,
	fill: props.element.fill || '#ffffff',
	stroke: props.element.stroke || '#5f6b7a',
	strokeWidth: props.element.strokeWidth || 2,
}));

const titleTextConfig = computed(() => ({
	x: 8,
	y: -30,
	width: Math.max(props.element.width - 16, 80),
	text: displayTitle.value,
	fontSize: 20,
	fontStyle: 'bold',
	fill: '#2c3e50',
	align: 'left',
	verticalAlign: 'middle',
	ellipsis: true,
}));

const handleDblClick = (e: any) => {
	e.evt.stopPropagation();

	if (!titleTextRef.value) {
		return;
	}

	const titleNode = titleTextRef.value.getNode();
	const stage = titleNode.getStage();
	if (!stage) return;

	const scale = stage.scaleX();
	const stageRect = stage.container().getBoundingClientRect();
	const titleRect = titleNode.getClientRect({ skipTransform: false });

	editingPos.left = `${stageRect.left + titleRect.x}px`;
	editingPos.top = `${stageRect.top + titleRect.y}px`;
	editingPos.width = `${Math.max(titleRect.width + 16, 200)}px`;
	editingPos.height = `${Math.max(titleRect.height + 8, 30)}px`;
	editingPos.fontSize = `${20 * scale}px`;

	initialTitle.value = props.element.title;
	editedTitle.value = props.element.title;
	isEditing.value = true;
	boardStore.setEditingElement(props.element.id);

	nextTick(() => {
		inputRef.value?.focus();
		inputRef.value?.select();
	});
};

const commitTitle = () => {
	isEditing.value = false;
	boardStore.setEditingElement(null);

	if (editedTitle.value !== initialTitle.value) {
		boardStore.updateElement(props.element.id, { title: editedTitle.value });
		historyStore.addState();
	}
};

const handleInput = () => {
	// Keep canvas title in sync while typing.
	editedTitle.value = editedTitle.value;
};

const handleInputBlur = () => {
	if (!isEditing.value) return;
	commitTitle();
};

const handleEnterKey = (e: KeyboardEvent) => {
	e.preventDefault();
	inputRef.value?.blur();
};

const handleEscapeKey = () => {
	isEditing.value = false;
	editedTitle.value = initialTitle.value;
	boardStore.setEditingElement(null);
};

const inputWrapperStyle = computed(() => ({
	position: 'fixed' as const,
	left: editingPos.left,
	top: editingPos.top,
	width: editingPos.width,
	height: editingPos.height,
	zIndex: '9999',
}));

const inputStyle = computed(() => ({
	width: '100%',
	height: '100%',
	fontSize: editingPos.fontSize,
	fontWeight: '700',
	color: '#2c3e50',
	background: 'rgba(255, 255, 255, 0.92)',
	border: '1px dashed #7f8c8d',
	borderRadius: '4px',
	padding: '2px 6px',
	margin: '0',
	boxSizing: 'border-box' as const,
	outline: 'none',
}));
</script>
