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
import type { BoardStoreState } from '@/stores/boardStore';
import { ElementType, type BoardElement, type DomainModelItemElement, type ConnectorElement, ConnectorTargetType, ConnectorAnchorSide, ConnectorLineType, ConnectorArrowType } from '@/types/elements';
import { useHistoryStore } from '@/stores/historyStore';
import { useTimerStore } from '@/stores/timerStore';
import Toolbar from '@/components/board/Toolbar.vue';
import MiroBoard from '@/components/board/MiroBoard.vue';
import DomainModelItemModal from '@/components/menu/DomainModelItemModal.vue';
import { loadStateFromLocalStorage } from '@/utils/localStorage';
import { boardApi } from '@/api';
import { getHexByName } from '@/constants/colors';
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';

const route = useRoute();
const boardId = route.params.boardId as string;
const boardStore = useBoardStore();
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
          // console.log('board event:', JSON.parse(message.body))
          // TODO: 等 payload 格式定案後再 parse / 更新 store
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
      attributes: item.attributes.map((attr: any) => ({
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
      defaultStickyNoteColor: '#ffeb3b',
      isDrawingConnector: boardStore.isDrawingConnector,
      connectorDrawingStart: boardStore.connectorDrawingStart,
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
  if (event.ctrlKey || event.metaKey) {
    if (event.key === 'z') {
      event.preventDefault();
      historyStore.undo();
    }
    if (event.key === 'y') {
      event.preventDefault();
      historyStore.redo();
    }
  }
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
