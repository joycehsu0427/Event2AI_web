import { inject, type ComputedRef, type InjectionKey, type Ref } from 'vue';
import type { Node as KonvaNode } from 'konva/lib/Node';

export type EditorCommitHandler = (value: string) => void;

export interface EditorPosition {
	left: string;
	top: string;
	width: string;
	height: string;
	fontSize: string;
}

export interface InlineEditorController {
	isEditing: Ref<boolean>;
	draftValue: Ref<string>;
	editingPos: EditorPosition;
	textareaWrapperStyle: ComputedRef<Record<string, string>>;
	inputWrapperStyle: ComputedRef<Record<string, string>>;
	textareaStyle: ComputedRef<Record<string, string>>;
	inputStyle: ComputedRef<Record<string, string>>;
	startEditing: (options: {
		anchorNode: KonvaNode | null;
		initialValue: string;
		fontSize: number;
		onCommit: EditorCommitHandler;
	}) => boolean;
	commitEditing: () => void;
	cancelEditing: () => void;
	handleEnterKey: (event: KeyboardEvent, allowMultiline?: boolean) => void;
}

export const boardElementEditorKey: InjectionKey<InlineEditorController> = Symbol('boardElementEditorKey');

export const useBoardElementEditor = (): InlineEditorController => {
	const controller = inject(boardElementEditorKey);
	if (!controller) {
		throw new Error('BoardElement editor controller is not available.');
	}
	return controller;
};