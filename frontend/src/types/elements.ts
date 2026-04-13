export enum ElementType {
  StickyNote = 'stickyNote',
  Text = 'text',
  Frame = 'frame',
  DomainEntity = 'domain-entity',
}

export interface BaseElement {
  id: string;
  type: ElementType;
  x: number;
  y: number;
  width: number;
  height: number;
  rotation?: number;
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
  frameId?: string | null;
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

export enum DomainEntityType {
  ENTITY = 'ENTITY',
  AGGREGATE = 'AGGREGATE',
  VALUE_OBJECT = 'VALUE_OBJECT',
  ENUM = 'ENUM',
}

export interface DomainEntityAttribute {
  name: string;
  dataType?: string;
  constraint?: string;
  displayOrder?: number;
}

export interface DomainEntityElement extends BaseElement {
  type: ElementType.DomainEntity;
  modelType: DomainEntityType;
  name: string;
  attributes: DomainEntityAttribute[];
}

// Union type for all possible elements
export type BoardElement = StickyNoteElement | TextElement | FrameElement | DomainEntityElement;
