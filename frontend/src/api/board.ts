import { apiClient, buildAuthHeaders } from './client';

export interface BoardMemberPayload {
	userEmail: string;
	role: 'OWNER' | 'EDITOR' | 'VIEWER';
}

export const boardApi = {
	async list() {
		const response = await apiClient.get('/boards', {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async create(payload: { title: string; description: string }) {
		const response = await apiClient.post('/boards', payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async update(boardId: string, payload: { title: string; description: string }) {
		const response = await apiClient.put(`/boards/${boardId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async remove(boardId: string) {
		const response = await apiClient.delete(`/boards/${boardId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async getComponents(boardId: string) {
		const response = await apiClient.get(`/boards/${boardId}/components`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async getMembers(boardId: string) {
		const response = await apiClient.get(`/boards/board_member/${boardId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async addMember(boardId: string, payload: BoardMemberPayload) {
		const response = await apiClient.post('/boards/board_member', {
			boardId,
			...payload,
		}, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async updateMember(boardId: string, payload: Omit<BoardMemberPayload, 'role'> & { role: 'EDITOR' | 'VIEWER' }) {
		const response = await apiClient.put(`/boards/board_member/${boardId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async removeMember(boardId: string, userId: string) {
		const response = await apiClient.delete(`/boards/board_member/${boardId}/${userId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},
};