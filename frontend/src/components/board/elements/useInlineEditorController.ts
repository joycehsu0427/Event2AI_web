import { computed, reactive, ref } from 'vue';
import type { Node as KonvaNode } from 'konva/lib/Node';
import { useBoardStore } from '@/stores/boardStore';
import type { EditorCommitHandler } from './boardElementContext';

export interface InlineEditorStyles {
	textarea: (editingPos: { fontSize: string }) => Record<string, string>;
	input: (editingPos: { fontSize: string }) => Record<string, string>;
}

export const useInlineEditorController = (options: {
	elementId: string;
	styles: InlineEditorStyles;
}) => {
	const boardStore = useBoardStore();
	const isEditing = ref(false);
	const draftValue = ref('');
	const initialValue = ref('');
	const commitHandler = ref<EditorCommitHandler | null>(null);

	const editingPos = reactive({
		left: '0px',
		top: '0px',
		width: '0px',
		height: '0px',
		fontSize: '14px',
	});

	const textareaWrapperStyle = computed(() => ({
		position: 'fixed' as const,
		left: editingPos.left,
		top: editingPos.top,
		width: editingPos.width,
		height: editingPos.height,
		zIndex: '9999',
	}));

	const inputWrapperStyle = computed(() => textareaWrapperStyle.value);
	const textareaStyle = computed(() => options.styles.textarea(editingPos));
	const inputStyle = computed(() => options.styles.input(editingPos));

	const startEditing = (editorOptions: {
		anchorNode: KonvaNode | null;
		initialValue: string;
		fontSize: number;
		onCommit: EditorCommitHandler;
	}) => {
		const { anchorNode, initialValue: startValue, fontSize, onCommit } = editorOptions;
		if (!anchorNode) {
			return false;
		}

		const stage = anchorNode.getStage();
		if (!stage) {
			return false;
		}

		const scale = stage.scaleX();
		const stageRect = stage.container().getBoundingClientRect();
		const anchorRect = anchorNode.getClientRect({ skipTransform: false });

		editingPos.left = `${stageRect.left + anchorRect.x}px`;
		editingPos.top = `${stageRect.top + anchorRect.y}px`;
		editingPos.width = `${anchorRect.width}px`;
		editingPos.height = `${anchorRect.height}px`;
		editingPos.fontSize = `${fontSize * scale}px`;

		initialValue.value = startValue;
		draftValue.value = startValue;
		commitHandler.value = onCommit;
		isEditing.value = true;
		boardStore.setEditingElement(options.elementId);

		return true;
	};

	const commitEditing = () => {
		if (!isEditing.value) {
			return;
		}

		isEditing.value = false;
		boardStore.setEditingElement(null);

		if (draftValue.value !== initialValue.value) {
			commitHandler.value?.(draftValue.value);
		}

		commitHandler.value = null;
	};

	const cancelEditing = () => {
		if (!isEditing.value) {
			return;
		}

		isEditing.value = false;
		draftValue.value = initialValue.value;
		commitHandler.value = null;
		boardStore.setEditingElement(null);
	};

	const handleEnterKey = (event: KeyboardEvent, allowMultiline = false) => {
		if (allowMultiline && event.shiftKey) {
			return;
		}

		event.preventDefault();
		commitEditing();
	};

	return {
		isEditing,
		draftValue,
		editingPos,
		textareaWrapperStyle,
		inputWrapperStyle,
		textareaStyle,
		inputStyle,
		startEditing,
		commitEditing,
		cancelEditing,
		handleEnterKey,
	};
};