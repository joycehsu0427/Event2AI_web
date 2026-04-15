import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';
import { ElementType, type BoardElement, type DomainModelItemElement } from '@/types/elements';
import type { CanvasTransform, BoardState } from '@/types/board';
import { saveStateToLocalStorage } from '@/utils/localStorage';
import { debounce } from '@/utils/debounce';
import router from '@/router';
import { stickyNoteApi, textBoxApi, frameApi, domainModelItemApi, connectorApi } from '@/api';
import { getNameByHex } from '@/constants/colors';

export interface BoardStoreState {
  elements: BoardElement[];
  selectedElementIds: string[];
  canvasTransform: CanvasTransform;
  editingElementId: string | null; // Tracks the ID of the element currently being edited via HTML
  editingDomainModelId: string | null; // ID of the Domain Model being edited in the modal
  isDomainModelModalOpen: boolean;
  // defaultStickyNoteColor: string; // New state for default sticky note color
  isDrawingConnector: boolean;
  connectorDrawingStart: {
    targetId?: string;
    targetType: any;
    point?: { x: number; y: number };
  } | null;
  clipboard: BoardElement[];
}

export const useBoardStore = defineStore('board', {
  state: (): BoardStoreState => ({
    elements: [],
    selectedElementIds: [],
    canvasTransform: { x: 0, y: 0, scale: 1 },
    editingElementId: null, // Initialize to null
    editingDomainModelId: null,
    isDomainModelModalOpen: false,
    // defaultStickyNoteColor: '#ffeb3b', // Default to yellow
    isDrawingConnector: false,
    connectorDrawingStart: null,
    clipboard: [],
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
    getEditingDomainModel: (state) => 
      state.elements.find(el => el.id === state.editingDomainModelId) as DomainModelItemElement | undefined,
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
      if (!boardId) return;
      
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
          await textBoxApi.update(element.id, payload);
        } catch (error) {
          console.error(`Failed to update text box ${element.id}:`, error);
        }
      }

      else if (element.type === ElementType.StickyNote) {
        const hasFrameIdUpdate = Boolean(
          updates && Object.prototype.hasOwnProperty.call(updates, 'frameId')
        );
        const frameIdPayload = hasFrameIdUpdate
          ? (element.frameId === null ? "null" : element.frameId)
          : null;

        const payload = {
          boardId,
          posX: element.x,
          posY: element.y,
          geoX: element.width,
          geoY: element.height,
          frameId: frameIdPayload,
          description: element.text,
          color: getNameByHex(element.backgroundColor), // Convert Hex to name for backend
          fontColor: element.textColor,
          fontSize: String(element.fontSize),
        };
        try {
          await stickyNoteApi.update(element.id, payload);
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
          await frameApi.update(element.id, payload);
        } catch (error) {
          console.error(`Failed to update frame ${element.id}:`, error);
        }
      }

      else if (element.type === ElementType.DomainModelItem) {
        const payload = {
          boardId,
          posX: element.x,
          posY: element.y,
          width: element.width,
          height: element.height,
          name: element.name,
          type: element.modelType,
          attributes: element.attributes,
        };
        try {
          await domainModelItemApi.update(element.id, payload);
        } catch (error) {
          console.error(`Failed to update domain model item ${element.id}:`, error);
        }
      }

      else if (element.type === ElementType.Connector) {
        const payload = {
          boardId,
          fromTargetType: element.fromTargetType,
          fromTargetId: element.fromTargetId,
          fromSide: element.fromSide,
          fromOffset: element.fromOffset,
          fromPoint: element.fromPoint,
          toTargetType: element.toTargetType,
          toTargetId: element.toTargetId,
          toSide: element.toSide,
          toOffset: element.toOffset,
          toPoint: element.toPoint,
          lineType: element.lineType,
          label: element.label,
          strokeColor: element.strokeColor,
          strokeWidth: element.strokeWidth,
          dashed: element.dashed,
          startArrow: element.startArrow,
          endArrow: element.endArrow,
          zIndex: element.zIndex,
        };
        try {
          await connectorApi.update(element.id, payload);
        } catch (error) {
          console.error(`Failed to update connector ${element.id}:`, error);
        }
      }
    },

    addElement(element: Omit<BoardElement, 'id'>, id?: string, autoSelect: boolean = true) {
      const newElement = { ...element, id: id ?? uuidv4() } as BoardElement;
      this.elements.push(newElement);
      if (autoSelect) {
        this.selectedElementIds = [newElement.id];
      }
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

    openDomainModelModal(id: string) {
      this.editingDomainModelId = id;
      this.isDomainModelModalOpen = true;
    },

    closeDomainModelModal() {
      this.isDomainModelModalOpen = false;
      this.editingDomainModelId = null;
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

    startDrawingConnector(start: { targetId?: string; targetType: any; point?: { x: number; y: number } }) {
      this.isDrawingConnector = true;
      this.connectorDrawingStart = start;
    },

    stopDrawingConnector() {
      this.isDrawingConnector = false;
      this.connectorDrawingStart = null;
    },

    copySelected() {
      const selected = this.getSelectedElements;
      if (selected.length === 0) return;

      const elementsToCopySet = new Set<BoardElement>();

      const addElementAndChildren = (el: BoardElement) => {
        if (elementsToCopySet.has(el)) return;
        
        // Don't copy connectors via normal copy/paste for now
        if (el.type === ElementType.Connector) return;

        elementsToCopySet.add(el);

        // If it's a frame, find all elements that are "inside" it
        if (el.type === ElementType.Frame) {
          this.elements.forEach(child => {
            // Check if child explicitly belongs to this frame
            if (child.frameId === el.id) {
              addElementAndChildren(child);
            } 
            // Also check spatial containment for elements that might not have frameId set correctly
            else if (
              child.type !== ElementType.Frame && 
              child.type !== ElementType.Connector &&
              child.x >= el.x &&
              child.x + child.width <= el.x + el.width &&
              child.y >= el.y &&
              child.y + child.height <= el.y + el.height
            ) {
              addElementAndChildren(child);
            }
          });
        }
      };

      selected.forEach(el => addElementAndChildren(el));

      if (elementsToCopySet.size > 0) {
        this.clipboard = JSON.parse(JSON.stringify(Array.from(elementsToCopySet)));
      }
    },

    cutSelected() {
      this.copySelected();
      // We don't delete here because deletion requires API calls which are handled in the View
      // The View will call deleteSelectedElements after calling this.
    },

    async paste() {
      if (this.clipboard.length === 0) return;

      const boardId = this.getCurrentBoardId();
      if (!boardId) return;

      const OFFSET = 30;
      const pastedIds: string[] = [];
      const oldToNewIdMap = new Map<string, string>();

      // Sort clipboard so frames are pasted first to establish new frame IDs
      const sortedClipboard = [...this.clipboard].sort((a, b) => {
        if (a.type === ElementType.Frame && b.type !== ElementType.Frame) return -1;
        if (a.type !== ElementType.Frame && b.type === ElementType.Frame) return 1;
        return 0;
      });

      for (const element of sortedClipboard) {
        let createdId: string | null = null;
        const newPos = {
          x: element.x + OFFSET,
          y: element.y + OFFSET,
        };

        // Determine the new frameId if the element belonged to a frame that was also copied
        const newFrameId = element.frameId ? (oldToNewIdMap.get(element.frameId) || null) : null;

        try {
          if (element.type === ElementType.StickyNote) {
            const res = await stickyNoteApi.create({
              boardId,
              posX: newPos.x,
              posY: newPos.y,
              geoX: element.width,
              geoY: element.height,
              description: element.text,
              color: getNameByHex(element.backgroundColor),
              fontColor: element.textColor,
              fontSize: element.fontSize,
              tag: 'sticky-note',
              frameId: newFrameId,
            });
            createdId = res.id;
          } else if (element.type === ElementType.Text) {
            const res = await textBoxApi.create({
              boardId,
              posX: newPos.x,
              posY: newPos.y,
              geoX: element.width,
              geoY: element.height,
              description: element.text,
              fontColor: element.textColor,
              fontSize: String(element.fontSize),
              frameID: newFrameId as any, // Text API uses frameID
            });
            createdId = res.id;
          } else if (element.type === ElementType.Frame) {
            const res = await frameApi.create({
              boardId,
              posX: newPos.x,
              posY: newPos.y,
              width: element.width,
              height: element.height,
              title: element.title,
            });
            createdId = res.id;
          } else if (element.type === ElementType.DomainModelItem) {
            const res = await domainModelItemApi.create({
              boardId,
              posX: newPos.x,
              posY: newPos.y,
              width: element.width,
              height: element.height,
              name: element.name,
              type: element.modelType,
              attributes: element.attributes,
              // DomainModelItem doesn't support frameId on backend yet
            });
            createdId = res.id;
          }

          if (createdId) {
            oldToNewIdMap.set(element.id, createdId);
            this.addElement(
              {
                ...element,
                x: newPos.x,
                y: newPos.y,
                frameId: newFrameId,
              } as any,
              createdId,
              false
            );
            pastedIds.push(createdId);
          }
        } catch (error) {
          console.error(`Failed to paste element ${element.id}:`, error);
        }
      }

      if (pastedIds.length > 0) {
        this.selectedElementIds = pastedIds;
        // Update clipboard positions and mapping for next paste
        this.clipboard.forEach((el) => {
          el.x += OFFSET;
          el.y += OFFSET;
          // Note: we don't update el.id or el.frameId in clipboard 
          // to keep them relative to the ORIGINAL clipboard state 
          // or we'd have to update the whole map.
          // Keeping them as is works for subsequent pastes since oldToNewIdMap is rebuilt.
        });
      }
    },
  },
});
