import { apiClient, buildAuthHeaders } from '../client';
import type { ConnectorTargetType, ConnectorAnchorSide, ConnectorLineType, ConnectorArrowType } from '@/types/elements';

export interface CreateConnectorRequest {
  boardId: string;
  frameId?: string;
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

export interface UpdateConnectorRequest {
  boardId?: string;
  frameId?: string;
  fromTargetType?: ConnectorTargetType;
  fromTargetId?: string;
  fromSide?: ConnectorAnchorSide;
  fromOffset?: number;
  fromPoint?: { x: number; y: number };
  toTargetType?: ConnectorTargetType;
  toTargetId?: string;
  toSide?: ConnectorAnchorSide;
  toOffset?: number;
  toPoint?: { x: number; y: number };
  lineType?: ConnectorLineType;
  label?: string;
  strokeColor?: string;
  strokeWidth?: number;
  dashed?: boolean;
  startArrow?: ConnectorArrowType;
  endArrow?: ConnectorArrowType;
  zIndex?: number;
}

export const connectorApi = {
  create: (request: CreateConnectorRequest) =>
    apiClient.post('/connectors', request, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  update: (id: string, request: UpdateConnectorRequest) =>
    apiClient.put(`/connectors/${id}`, request, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  delete: (id: string) =>
    apiClient.delete(`/connectors/${id}`, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  getByBoardId: (boardId: string) =>
    apiClient.get(`/connectors?boardId=${boardId}`, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),
};
