<template>
	<v-rect :config="rectConfig" />
	<v-text
		ref="titleTextRef"
		:config="titleTextConfig"
		@dblclick="handleDblClick"
		@dbltap="handleDblClick"
		v-show="!isEditing"
	/>

	<Teleport to="body">
		<div v-if="isEditing" :style="inputWrapperStyle">
			<input
				ref="inputRef"
				v-model="draftValue"
				type="text"
				:style="inputStyle"
				@blur="handleInputBlur"
				@keydown.enter="handleEnterKey"
				@keydown.esc="handleEscapeKey"
			/>
		</div>
	</Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import type { FrameElement } from '@/interfaces/elements';
import { useBoardStore } from '@/stores/boardStore';
import { useHistoryStore } from '@/stores/historyStore';
import { useBoardElementEditor } from './boardElementContext';

const props = defineProps<{
	element: FrameElement;
}>();

const boardStore = useBoardStore();
const historyStore = useHistoryStore();
const editor = useBoardElementEditor();
const {
	isEditing,
	draftValue,
	inputWrapperStyle,
	inputStyle,
	startEditing,
	commitEditing,
	cancelEditing,
} = editor;

const titleTextRef = ref<InstanceType<any> | null>(null);
const inputRef = ref<HTMLInputElement | null>(null);

watch(
	() => props.element.title,
	(title) => {
		if (!isEditing.value) {
			draftValue.value = title;
		}
	}
);

const displayTitle = computed(() => (isEditing.value ? draftValue.value : props.element.title));

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
	if (!titleNode.getStage()) {
		return;
	}

	if (!startEditing({
		anchorNode: titleNode,
		initialValue: props.element.title,
		fontSize: 20,
		onCommit: (value) => {
			boardStore.updateElement(props.element.id, { title: value });
			historyStore.addState();
		},
	})) {
		return;
	}

	nextTick(() => {
		inputRef.value?.focus();
		inputRef.value?.select();
	});
};

const handleInputBlur = () => {
	commitEditing();
};

const handleEnterKey = (e: KeyboardEvent) => {
	e.preventDefault();
	commitEditing();
};

const handleEscapeKey = () => {
	cancelEditing();
};
</script>
