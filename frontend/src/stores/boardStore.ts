import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';
import type { BoardElement } from '@/interfaces/elements';
import { ElementType } from '@/interfaces/elements'; // Import ElementType as a value
import type { CanvasTransform, BoardState } from '@/interfaces/board';
import { saveStateToLocalStorage } from '@/utils/localStorage';
import { debounce } from '@/utils/debounce';
import router from '@/router';
import { elementApi } from '@/api';
import { getNameByHex } from '@/constants/colors';

export interface BoardStoreState {
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
    getCurrentBoardId(): string | null {
      const routeBoardId = router.currentRoute.value.params.boardId;
      return typeof routeBoardId === 'string' && routeBoardId.trim() !== ''
        ? routeBoardId
        : null;
    },

    async syncElementUpdateToBackend(element: BoardElement, updates?: Partial<BoardElement>) {
      const boardId = this.getCurrentBoardId();
      
      if (element.type === ElementType.Text) {
        const payload = {
          boardId,
          posX: element.x,
          posY: element.y,
          geoX: element.width,
          geoY: element.height,
          description: element.text,
          fontColor: element.textColor,
          fontSize: String(element.fontSize),
        };
        try {
          await elementApi.updateTextBox(element.id, payload);
        } catch (error) {
          console.error(`Failed to update text box ${element.id}:`, error);
        }
      }

      else if (element.type === ElementType.StickyNote) {
        const hasFrameIdUpdate = Boolean(
          updates && Object.prototype.hasOwnProperty.call(updates, 'frameId')
        );
        const frameIDPayload = hasFrameIdUpdate
          ? (element.frameId === null ? "null" : element.frameId)
          : null;

        const payload = {
          boardId,
          posX: element.x,
          posY: element.y,
          geoX: element.width,
          geoY: element.height,
          frameID: frameIDPayload,
          description: element.text,
          color: getNameByHex(element.backgroundColor), // Convert Hex to name for backend
          fontColor: element.textColor,
          fontSize: String(element.fontSize),
        };
        try {
          await elementApi.updateStickyNote(element.id, payload);
          // console.log("Updated sticky note", res.data);
        } catch (error) {
          console.error(`Failed to update sticky note ${element.id}:`, error);
        }
      }

      else if (element.type === ElementType.Frame) {
        const payload = {
          boardId,
          posX: element.x,
          posY: element.y,
          width: element.width,
          height: element.height,
          title: element.title
        };
        try {
          await elementApi.updateFrame(element.id, payload);
        } catch (error) {
          console.error(`Failed to update frame ${element.id}:`, error);
        }
      }
    },

    addElement(element: Omit<BoardElement, 'id'>, id?: string) {
      const newElement = { ...element, id: id ?? uuidv4() } as BoardElement;
      this.elements.push(newElement);
      this.selectedElementIds = [newElement.id]; // Select the newly added element
    },

    updateElement(id: string, updates: Partial<BoardElement>) {
      const index = this.elements.findIndex((el) => el.id === id);
      if (index !== -1) {
        const currentElement = this.elements[index];
        if (!currentElement) return;

        const updatedElement = { ...currentElement, ...updates } as BoardElement;
        this.elements[index] = updatedElement;
        void this.syncElementUpdateToBackend(updatedElement, updates);
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