import { apiClient, buildAuthHeaders } from '../client';
import type { DomainModelItemType, DomainModelAttribute } from '@/types/elements';

export interface CreateDomainModelItemRequest {
  boardId: string;
  posX: number;
  posY: number;
  width: number;
  height: number;
  name: string;
  type: DomainModelItemType;
  description?: string;
  attributes: DomainModelAttribute[];
}

export interface UpdateDomainModelItemRequest {
  boardId?: string;
  posX?: number;
  posY?: number;
  width?: number;
  height?: number;
  name?: string;
  type?: DomainModelItemType;
  description?: string;
  attributes?: DomainModelAttribute[];
}

export const domainModelItemApi = {
  create: (request: CreateDomainModelItemRequest) =>
    apiClient.post('/domain-model-items', request, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  update: (id: string, request: UpdateDomainModelItemRequest) =>
    apiClient.put(`/domain-model-items/${id}`, request, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  delete: (id: string) =>
    apiClient.delete(`/domain-model-items/${id}`, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  getByBoardId: (boardId: string) =>
    apiClient.get(`/domain-model-items?boardId=${boardId}`, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),
};
