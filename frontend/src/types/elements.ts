export enum ElementType {
  StickyNote = 'stickyNote',
  Text = 'text',
  Frame = 'frame',
  DomainModelItem = 'domain-model-item',
  Connector = 'connector',
}

export enum DomainModelItemType {
  ENTITY = 'ENTITY',
  AGGREGATE = 'AGGREGATE',
  VALUE_OBJECT = 'VALUE_OBJECT',
  ENUM = 'ENUM',
}

export interface DomainModelAttribute {
  name: string;
  dataType?: string;
  constraint?: string;
  displayOrder?: number;
}

export interface DomainModelItemElement extends BaseElement {
  type: ElementType.DomainModelItem;
  modelType: DomainModelItemType;
  name: string;
  attributes: DomainModelAttribute[];
}

export enum ConnectorTargetType {
  STICKY_NOTE = 'STICKY_NOTE',
  TEXT_BOX = 'TEXT_BOX',
  FRAME = 'FRAME',
  DOMAIN_MODEL_ITEM = 'DOMAIN_MODEL_ITEM',
  FREE_POINT = 'FREE_POINT',
}

export enum ConnectorAnchorSide {
  TOP = 'TOP',
  RIGHT = 'RIGHT',
  BOTTOM = 'BOTTOM',
  LEFT = 'LEFT',
}

export enum ConnectorLineType {
  STRAIGHT = 'STRAIGHT',
  ORTHOGONAL = 'ORTHOGONAL',
}

export enum ConnectorArrowType {
  NONE = 'NONE',
  TRIANGLE = 'TRIANGLE',
}

export interface ConnectorElement extends Omit<BaseElement, 'x' | 'y' | 'width' | 'height'> {
  type: ElementType.Connector;
  fromTargetType: ConnectorTargetType;
  fromTargetId?: string;
  fromSide?: ConnectorAnchorSide;
  fromOffset?: number;
  fromPoint?: { x: number; y: number };
  toTargetType: ConnectorTargetType;
  toTargetId?: string;
  toSide?: ConnectorAnchorSide;
  toOffset?: number;
  toPoint?: { x: number; y: number };
  lineType: ConnectorLineType;
  label?: string;
  strokeColor: string;
  strokeWidth: number;
  dashed: boolean;
  startArrow: ConnectorArrowType;
  endArrow: ConnectorArrowType;
  zIndex: number;
}

export interface BaseElement {
  id: string;
  type: ElementType;
  x: number;
  y: number;
  width: number;
  height: number;
  rotation?: number;
  frameId?: string | null;
  // Common visual properties
  fill?: string;
  stroke?: string;
  strokeWidth?: number;
  draggable?: boolean;
  selected?: boolean; // Managed by boardStore, but useful in element data model
}

export interface StickyNoteElement extends BaseElement {
  type: ElementType.StickyNote;
  text: string;
  fontSize: number;
  textColor: string;
  backgroundColor: string;
}

export interface TextElement extends BaseElement {
  type: ElementType.Text;
  text: string;
  fontSize: number;
  fontFamily: string;
  textColor: string;
}

export interface FrameElement extends BaseElement {
  type: ElementType.Frame;
  title: string;
}

// Union type for all possible elements
export type BoardElement = StickyNoteElement | TextElement | FrameElement | DomainModelItemElement | ConnectorElement;
