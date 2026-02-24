import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';
import type { BoardElement } from '@/interfaces/elements';
import { ElementType } from '@/interfaces/elements'; // Import ElementType as a value
import type { CanvasTransform, BoardState } from '@/interfaces/board';
import { saveStateToLocalStorage } from '@/utils/localStorage';
import { debounce } from '@/utils/debounce';

interface BoardStoreState {
  elements: BoardElement[];
  selectedElementIds: string[];
  canvasTransform: CanvasTransform;
  editingElementId: string | null; // Tracks the ID of the element currently being edited via HTML
  defaultStickyNoteColor: string; // New state for default sticky note color
}

export const useBoardStore = defineStore('board', {
  state: (): BoardStoreState => ({
    elements: [],
    selectedElementIds: [],
    canvasTransform: { x: 0, y: 0, scale: 1 },
    editingElementId: null, // Initialize to null
    defaultStickyNoteColor: '#ffeb3b', // Default to yellow
  }),
  getters: {
    getElements: (state) => state.elements,
    getSelectedElements: (state) =>
      state.elements.filter((el) => state.selectedElementIds.includes(el.id)),
    getCanvasTransform: (state) => state.canvasTransform,
    getElementById: (state) => (id: string) =>
      state.elements.find((el) => el.id === id),
    getDefaultStickyNoteColor: (state) => state.defaultStickyNoteColor, // New getter
    getEditingElementId: (state) => state.editingElementId, // New getter
  },
  actions: {
    addElement(element: Omit<BoardElement, 'id'>) {
      const newElement: BoardElement = { ...element, id: uuidv4() };
      this.elements.push(newElement);
      this.selectedElementIds = [newElement.id]; // Select the newly added element
    },

    updateElement(id: string, updates: Partial<BoardElement>) {
      const index = this.elements.findIndex((el) => el.id === id);
      if (index !== -1) {
        this.elements[index] = { ...this.elements[index], ...updates };
      }
    },

    updateSelectedElementsColor(color: string) {
      this.selectedElementIds.forEach(id => {
        const element = this.getElementById(id);
        if (element && element.type === ElementType.StickyNote) {
          this.updateElement(id, { backgroundColor: color });
        }
      });
    },

    setDefaultStickyNoteColor(color: string) {
      this.defaultStickyNoteColor = color;
    },

    setEditingElement(id: string | null) {
      this.editingElementId = id;
    },

    deleteElements(ids: string[]) {
      this.elements = this.elements.filter((el) => !ids.includes(el.id));
      this.selectedElementIds = this.selectedElementIds.filter(
        (id) => !ids.includes(id)
      ); // Deselect deleted elements
    },

    selectElement(id: string | null, multiple: boolean = false) {
      if (id === null) {
        this.selectedElementIds = [];
      } else {
        const index = this.selectedElementIds.indexOf(id);
        if (multiple) {
          if (index === -1) {
            this.selectedElementIds.push(id);
          } else {
            this.selectedElementIds.splice(index, 1);
          }
        } else {
          this.selectedElementIds = [id];
        }
      }
    },

    setCanvasTransform(transform: Partial<CanvasTransform>) {
      this.canvasTransform = { ...this.canvasTransform, ...transform };
    },

    loadBoardState(state: BoardState) {
      this.elements = state.elements;
      this.canvasTransform = state.canvasTransform;
      this.selectedElementIds = []; // Clear selection on load
    },

    getCurrentBoardState(): BoardState {
      return {
        elements: JSON.parse(JSON.stringify(this.elements)),
        canvasTransform: { ...this.canvasTransform },
      };
    },

    debouncedSaveState: debounce(function(this: any) {
        const boardStore = useBoardStore();
        saveStateToLocalStorage(boardStore.getCurrentBoardState());
    }, 1000),

    saveBoardStateToLocalStorage() {
      this.debouncedSaveState();
    },
  },
});