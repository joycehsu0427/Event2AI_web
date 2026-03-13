import type { ElementType } from '@/interfaces/elements';

export interface ElementViewportRect {
  left: number;
  top: number;
  width: number;
  height: number;
}

export interface ElementEditRequest {
  elementId: string;
  elementType: ElementType;
  text: string;
  viewportRect: ElementViewportRect;
  fontSize: number;
  fontFamily: string;
  textColor: string;
  backgroundColor: string;
  lineHeight: number;
}
