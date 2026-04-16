<template>
  <div class="board-view">
    <Toolbar class="board-toolbar" />
    <MiroBoard class="miro-board-container" @contextmenu.prevent="() => {}"/>
    <DomainModelItemModal />
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useBoardStore } from '@/stores/boardStore';
import { useAuthStore } from '@/stores/authStore';
import type { BoardStoreState } from '@/stores/boardStore';
import { ElementType, ConnectorTargetType, ConnectorAnchorSide, ConnectorLineType, ConnectorArrowType, type BoardElement, type DomainModelItemElement, type ConnectorElement } from '@/types/elements';
import { useHistoryStore } from '@/stores/historyStore';
import { useTimerStore } from '@/stores/timerStore';
import Toolbar from '@/components/board/Toolbar.vue';
import MiroBoard from '@/components/board/MiroBoard.vue';
import DomainModelItemModal from '@/components/menu/DomainModelItemModal.vue';
import { loadStateFromLocalStorage } from '@/utils/localStorage';
import { handleBoardWebSocketEvent } from '@/utils/boardWebSocket';
import { boardApi, stickyNoteApi, textBoxApi, frameApi, domainModelItemApi, connectorApi } from '@/api';
import { getHexByName } from '@/constants/colors';
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';

const route = useRoute();
const boardId = route.params.boardId as string;
const boardStore = useBoardStore();
const authStore = useAuthStore();
const historyStore = useHistoryStore();
const timerStore = useTimerStore();
// const POLLING_INTERVAL_MS = 5000;
const stompClient = ref<Client | null>(null);

let boardSubscription: StompSubscription | null = null;

function connectBoardTopic(boardId: string) {
  const token = localStorage.getItem('token')
  if (!token) {
    console.error('Missing JWT token')
    return
  }

  const client = new Client({
    brokerURL: "ws://localhost:8080/ws",
    reconnectDelay: 5000,
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    onConnect: () => {
      console.log("websocket connected!")
      boardSubscription = client.subscribe(
        `/topic/boards/${boardId}/events`,
        (message: IMessage) => {
          const event = JSON.parse(message.body);
          handleBoardWebSocketEvent(boardStore, event, authStore.currentUser?.id);
        },
      )
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message'], frame.body)
    },
    onWebSocketError: (event) => {
      console.error('WebSocket error:', event)
    },
  })

  client.activate()
  stompClient.value = client
}

function disconnectBoardTopic() {
  boardSubscription?.unsubscribe()
  boardSubscription = null
  stompClient.value?.deactivate()
  stompClient.value = null
  console.log("websocket disconnected!")
}

async function fetchBoardData(boardId: string) {
  try {
    const data = await boardApi.getComponents(boardId);

    const stickyNoteElements: BoardElement[] = (data?.stickyNotes ?? []).map((note: any) => ({
      id: note.id,
      type: ElementType.StickyNote,
      x: note.posX,
      y: note.posY,
      width: note.geoX,
      height: note.geoY,
      frameId: note.frameId || null,
      text: note.description,
      fontSize: Number(note.fontSize) || 20,
      textColor: note.fontColor || '#000000',
      backgroundColor: getHexByName(note.color),
      draggable: true,
    }));

    const textElements: BoardElement[] = (data?.textBoxes ?? []).map((text: any) => ({
      id: text.id,
      type: ElementType.Text,
      x: text.posX,
      y: text.posY,
      width: text.geoX,
      height: text.geoY,
      text: text.description,
      fontSize: Number(text.fontSize) || 24,
      fontFamily: 'Arial',
      textColor: text.fontColor || '#000000',
      draggable: true,
    }));

    const frameElements: BoardElement[] = (data?.frames ?? []).map((frame: any) => ({
      id: frame.id,
      type: ElementType.Frame,
      x: frame.posX,
      y: frame.posY,
      width: frame.width,
      height: frame.height,
      title: frame.title,
      draggable: true,
    }));

    const domainModelItemElements: BoardElement[] = (data?.domainModelItems ?? []).map((item: any) => ({
      id: item.id,
      type: ElementType.DomainModelItem,
      x: item.posX,
      y: item.posY,
      width: item.width,
      height: item.height,
      name: item.name,
      modelType: item.type,
      attributes: (item.attributes ?? []).map((attr: any) => ({
        name: attr.name,
        dataType: attr.dataType,
        constraint: attr.constraint,
        displayOrder: attr.displayOrder,
      })),
      draggable: true,
    }));

    const connectorElements: BoardElement[] = (data?.connectors ?? []).map((conn: any) => ({
      id: conn.id,
      type: ElementType.Connector,
      fromTargetType: conn.fromTargetType,
      fromTargetId: conn.fromTargetId,
      fromSide: conn.fromSide,
      fromOffset: conn.fromOffset,
      fromPoint: conn.fromPoint,
      toTargetType: conn.toTargetType,
      toTargetId: conn.toTargetId,
      toSide: conn.toSide,
      toOffset: conn.toOffset,
      toPoint: conn.toPoint,
      lineType: conn.lineType,
      label: conn.label,
      strokeColor: conn.strokeColor || '#333333',
      strokeWidth: conn.strokeWidth || 2,
      dashed: conn.dashed || false,
      startArrow: conn.startArrow || ConnectorArrowType.NONE,
      endArrow: conn.endArrow || ConnectorArrowType.TRIANGLE,
      zIndex: conn.zIndex || 0,
      draggable: false,
    }));

    const canvasTransform = boardStore.canvasTransform;

    const state: BoardStoreState = {
      elements: [
        ...stickyNoteElements,
        ...textElements,
        ...frameElements,
        ...domainModelItemElements,
        ...connectorElements
      ],
      selectedElementIds: boardStore.selectedElementIds,
      canvasTransform: canvasTransform,
      editingElementId: boardStore.getEditingElementId,
      editingDomainModelId: boardStore.editingDomainModelId,
      isDomainModelModalOpen: boardStore.isDomainModelModalOpen,
      isDrawingConnector: boardStore.isDrawingConnector,
      connectorDrawingStart: boardStore.connectorDrawingStart,
      clipboard: boardStore.clipboard,
    };
    boardStore.loadBoardState(state);
  } catch (error) {
    console.error('Error fetching board data:', error);
  }
}

onMounted(() => {
  connectBoardTopic(boardId);
  fetchBoardData(boardId);
  const savedState = loadStateFromLocalStorage();
  if (savedState) {
    boardStore.loadBoardState(savedState);
  }
  historyStore.initializeHistory();
  window.addEventListener('keydown', handleKeyDown);

  // timerStore.start(async () => {
  //   await fetchBoardData(boardId);
  // }, POLLING_INTERVAL_MS);
});

onUnmounted(() => {
  disconnectBoardTopic();
  window.removeEventListener('keydown', handleKeyDown);
  timerStore.stop();
});

watch(
  () => boardStore.getCurrentBoardState(),
  () => {
    if (historyStore.isApplyingHistory) {
      return;
    }
    boardStore.saveBoardStateToLocalStorage();
  },
  { deep: true }
);


const handleKeyDown = (event: KeyboardEvent) => {
  // Disable global shortcuts if we're in inline editing mode
  if (boardStore.getEditingElementId !== null) return;

  // Disable global shortcuts if focus is on an input or textarea (like in a modal)
  const activeElement = document.activeElement;
  if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA')) {
    return;
  }

  if (event.ctrlKey || event.metaKey) {
    if (event.key === 'z') {
      event.preventDefault();
      historyStore.undo();
    } else if (event.key === 'y') {
      event.preventDefault();
      historyStore.redo();
    } else if (event.key === 'c') {
      // No preventDefault here to allow normal copy if needed elsewhere, 
      // but usually we want to copy the selected elements.
      boardStore.copySelected();
    } else if (event.key === 'x') {
      // Ctrl+X: Cut
      boardStore.cutSelected();
      deleteSelectedElements();
    } else if (event.key === 'v') {
      event.preventDefault();
      boardStore.paste();
    }
  } else if (event.key === 'Delete' || event.key === 'Backspace') {
    if (boardStore.selectedElementIds.length > 0) {
      // Using Toolbar's delete logic as reference (but it's private there, so we re-implement or call store)
      // Actually MiroBoard/Toolbar should probably share this logic.
      // For now, let's trigger the deletion from the store.
      const idsToDelete = [...boardStore.selectedElementIds];
      
      // We should ideally call the API for each deletion if we want it to be persistent.
      // Since I don't have a single "deleteAll" API, I'll rely on the user having 
      // already established how to delete elements.
      // Looking at boardStore.ts, it has deleteElements(ids) which only updates local state.
      
      // I'll search for how Toolbar deletes elements to stay consistent.
      deleteSelectedElements();
    }
  }
};

const deleteSelectedElements = async () => {
  const initialIds = [...boardStore.selectedElementIds];
  if (initialIds.length === 0) return;

  const allIdsToDelete = new Set<string>();

  const collectIds = (id: string) => {
    if (allIdsToDelete.has(id)) return;
    allIdsToDelete.add(id);

    const el = boardStore.getElementById(id);
    if (el && el.type === ElementType.Frame) {
      boardStore.elements.forEach(child => {
        // Check if child explicitly belongs to this frame
        if (child.frameId === el.id) {
          collectIds(child.id);
        } 
        // Also check spatial containment to be consistent with copy logic
        else if (
          child.type !== ElementType.Frame && 
          child.type !== ElementType.Connector &&
          child.x >= el.x &&
          child.x + child.width <= el.x + el.width &&
          child.y >= el.y &&
          child.y + child.height <= el.y + el.height
        ) {
          collectIds(child.id);
        }
      });
    }
  };

  initialIds.forEach(id => collectIds(id));
  const idsArr = Array.from(allIdsToDelete);

  for (const id of idsArr) {
    const element = boardStore.getElementById(id);
    if (!element) continue;

    try {
      if (element.type === ElementType.StickyNote) {
        await stickyNoteApi.delete(id);
      } else if (element.type === ElementType.Text) {
        await textBoxApi.delete(id);
      } else if (element.type === ElementType.Frame) {
        await frameApi.delete(id);
      } else if (element.type === ElementType.DomainModelItem) {
        await domainModelItemApi.delete(id);
      } else if (element.type === ElementType.Connector) {
        await connectorApi.delete(id);
      }
    } catch (error) {
      console.error(`Failed to delete element ${id}:`, error);
    }
  }
  boardStore.deleteElements(idsArr);
  historyStore.addState();
};
</script>

<style scoped>
.board-view {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.board-toolbar {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  background-color: #f0f0f0;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.miro-board-container {
  flex-grow: 1;
  background-color: #f5f5f5;
}
</style>
