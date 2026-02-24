import type { BoardElement } from './elements';

export interface CanvasTransform {
  x: number;
  y: number;
  scale: number;
}

export interface BoardState {
  elements: BoardElement[];
  canvasTransform: CanvasTransform;
  // Add other global board settings if necessary
}
