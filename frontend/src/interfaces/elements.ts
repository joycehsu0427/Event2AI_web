export enum ElementType {
  StickyNote = 'stickyNote',
  Text = 'text',
  // Add more types as needed (e.g., Rectangle, Circle, Image)
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
  // ... potentially more common properties
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
  // ... potentially more text-specific properties
}

// Union type for all possible elements
export type BoardElement = StickyNoteElement | TextElement;
