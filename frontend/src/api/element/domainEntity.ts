import { apiClient, buildAuthHeaders } from '../client';
import type { DomainEntityType, DomainEntityAttribute } from '@/types/elements';

export interface CreateDomainEntityRequest {
  boardId: string;
  posX: number;
  posY: number;
  width: number;
  height: number;
  name: string;
  type: DomainEntityType;
  description?: string;
  attributes: DomainEntityAttribute[];
}

export interface UpdateDomainEntityRequest {
  boardId?: string;
  posX?: number;
  posY?: number;
  width?: number;
  height?: number;
  name?: string;
  type?: DomainEntityType;
  description?: string;
  attributes?: DomainEntityAttribute[];
}

export const domainEntityApi = {
  create: (request: CreateDomainEntityRequest) =>
    apiClient.post('/domain-entities', request, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  update: (id: string, request: UpdateDomainEntityRequest) =>
    apiClient.put(`/domain-entities/${id}`, request, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  delete: (id: string) =>
    apiClient.delete(`/domain-entities/${id}`, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),

  getByBoardId: (boardId: string) =>
    apiClient.get(`/domain-entities?boardId=${boardId}`, {
        headers: buildAuthHeaders(),
    }).then(res => res.data),
};
