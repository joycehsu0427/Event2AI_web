import { ElementType, type BoardElement } from '@/types/elements';

export interface BoardWebSocketEvent {
  userId?: string | null;
  type: string;
  payload: any;
}

export interface BoardWebSocketStore {
  elements: BoardElement[];
  addElement(element: Omit<BoardElement, 'id'>, id?: string, autoSelect?: boolean): void;
  deleteElements(ids: string[]): void;
}

const colorNameToHex: Record<string, string> = {
  blue: '#1E90FF',
  red: '#FF6B6B',
  yellow: '#FFEB3B',
  green: '#4CAF50',
  purple: '#9C27B0',
  pink: '#FF69B4',
  orange: '#FF9800',
  gray: '#757575',
};

function hexByColorName(colorName: string): string {
  return colorNameToHex[colorName.toLowerCase()] || '#FFEB3B';
}

function updateElementLocal(store: BoardWebSocketStore, id: string, updates: Partial<BoardElement>) {
  const index = store.elements.findIndex((element) => element.id === id);
  if (index === -1) return;

  const currentElement = store.elements[index];
  if (!currentElement) return;

  store.elements[index] = { ...currentElement, ...updates } as BoardElement;
}

function createRemoteElement(store: BoardWebSocketStore, element: string, payload: any) {
  if (element === 'stickyNote') {
    store.addElement(
      {
        type: ElementType.StickyNote,
        x: payload.posX,
        y: payload.posY,
        width: payload.geoX,
        height: payload.geoY,
        frameId: payload.frameId || null,
        text: payload.description,
        fontSize: Number(payload.fontSize) || 20,
        textColor: payload.fontColor || '#000000',
        backgroundColor: payload.color ? hexByColorName(payload.color) : '#FFEB3B',
        draggable: true,
        zIndex: payload.zIndex || 0,
      } as Omit<BoardElement, 'id'>,
      payload.id,
      false
    );
    console.log('Remote sticky note created:', payload.id);
    return;
  }

  if (element === 'textBox') {
    store.addElement(
      {
        type: ElementType.Text,
        x: payload.posX,
        y: payload.posY,
        width: payload.geoX,
        height: payload.geoY,
        frameId: payload.frameID || null,
        text: payload.description,
        fontSize: Number(payload.fontSize) || 24,
        textColor: payload.fontColor || '#000000',
        backgroundColor: '#FFFFFF',
        draggable: true,
        zIndex: payload.zIndex || 0,
      } as Omit<BoardElement, 'id'>,
      payload.id,
      false
    );
    console.log('Remote text box created:', payload.id);
    return;
  }

  if (element === 'frame') {
    store.addElement(
      {
        type: ElementType.Frame,
        x: payload.posX,
        y: payload.posY,
        width: payload.width,
        height: payload.height,
        title: payload.title || 'Frame',
        draggable: true,
        zIndex: payload.zIndex || 0,
      } as Omit<BoardElement, 'id'>,
      payload.id,
      false
    );
    console.log('Remote frame created:', payload.id);
    return;
  }

  if (element === 'domainModelItem') {
    store.addElement(
      {
        type: ElementType.DomainModelItem,
        x: payload.posX,
        y: payload.posY,
        width: payload.width,
        height: payload.height,
        modelType: payload.type,
        name: payload.name,
        attributes: payload.attributes || [],
        draggable: true,
        zIndex: payload.zIndex || 0,
      } as Omit<BoardElement, 'id'>,
      payload.id,
      false
    );
    console.log('Remote domain model item created:', payload.id);
    return;
  }

  if (element === 'connector') {
    store.addElement(
      {
        type: ElementType.Connector,
        fromTargetType: payload.fromTargetType,
        fromTargetId: payload.fromTargetId,
        fromSide: payload.fromSide,
        fromOffset: payload.fromOffset,
        fromPoint: payload.fromPoint,
        toTargetType: payload.toTargetType,
        toTargetId: payload.toTargetId,
        toSide: payload.toSide,
        toOffset: payload.toOffset,
        toPoint: payload.toPoint,
        lineType: payload.lineType,
        label: payload.label,
        strokeColor: payload.strokeColor,
        strokeWidth: payload.strokeWidth,
        dashed: payload.dashed,
        startArrow: payload.startArrow,
        endArrow: payload.endArrow,
        zIndex: payload.zIndex || 0,
      } as Omit<BoardElement, 'id'>,
      payload.id,
      false
    );
    console.log('Remote connector created:', payload.id);
  }
}

function updateRemoteElement(store: BoardWebSocketStore, element: string, payload: any) {
  const existingElement = store.elements.find((boardElement) => boardElement.id === payload.id);
  if (!existingElement) {
    createRemoteElement(store, element, payload);
    return;
  }

  if (element === 'stickyNote') {
    updateElementLocal(store, payload.id, {
      x: payload.posX,
      y: payload.posY,
      width: payload.geoX,
      height: payload.geoY,
      text: payload.description,
      textColor: payload.fontColor,
      fontSize: Number(payload.fontSize) || 20,
      backgroundColor: payload.color ? hexByColorName(payload.color) : undefined,
      frameId: payload.frameId || null,
      zIndex: payload.zIndex,
    });
    console.log('Remote sticky note updated:', payload.id);
    return;
  }

  if (element === 'textBox') {
    updateElementLocal(store, payload.id, {
      x: payload.posX,
      y: payload.posY,
      width: payload.geoX,
      height: payload.geoY,
      text: payload.description,
      textColor: payload.fontColor,
      fontSize: Number(payload.fontSize) || 24,
      frameId: payload.frameID || null,
      zIndex: payload.zIndex,
    });
    console.log('Remote text box updated:', payload.id);
    return;
  }

  if (element === 'frame') {
    updateElementLocal(store, payload.id, {
      x: payload.posX,
      y: payload.posY,
      width: payload.width,
      height: payload.height,
      title: payload.title,
      zIndex: payload.zIndex,
    });
    console.log('Remote frame updated:', payload.id);
    return;
  }

  if (element === 'domainModelItem') {
    updateElementLocal(store, payload.id, {
      x: payload.posX,
      y: payload.posY,
      width: payload.width,
      height: payload.height,
      modelType: payload.type,
      name: payload.name,
      attributes: payload.attributes || [],
      zIndex: payload.zIndex,
    });
    console.log('Remote domain model item updated:', payload.id);
    return;
  }

  if (element === 'connector') {
    updateElementLocal(store, payload.id, {
      fromTargetType: payload.fromTargetType,
      fromTargetId: payload.fromTargetId,
      fromSide: payload.fromSide,
      fromOffset: payload.fromOffset,
      fromPoint: payload.fromPoint,
      toTargetType: payload.toTargetType,
      toTargetId: payload.toTargetId,
      toSide: payload.toSide,
      toOffset: payload.toOffset,
      toPoint: payload.toPoint,
      lineType: payload.lineType,
      label: payload.label,
      strokeColor: payload.strokeColor,
      strokeWidth: payload.strokeWidth,
      dashed: payload.dashed,
      startArrow: payload.startArrow,
      endArrow: payload.endArrow,
      zIndex: payload.zIndex,
    });
    console.log('Remote connector updated:', payload.id);
  }
}

function deleteRemoteElement(store: BoardWebSocketStore, payload: any) {
  const elementId = payload.id;
  store.deleteElements([elementId]);
  console.log('Remote element deleted:', elementId);
}

export function handleBoardWebSocketEvent(
  store: BoardWebSocketStore,
  event: BoardWebSocketEvent,
  currentUserId: string | null | undefined
) {
  if (event.userId === currentUserId) {
    return;
  }

  const { type, payload } = event;
  const [element, behaviour] = type.split('.');

  try {
    if (behaviour === 'created') {
      createRemoteElement(store, element, payload);
    } else if (behaviour === 'updated') {
      updateRemoteElement(store, element, payload);
    } else if (behaviour === 'deleted') {
      deleteRemoteElement(store, payload);
    }
  } catch (error) {
    console.error('Error handling WebSocket event:', error);
  }
}