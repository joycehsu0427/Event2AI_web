import { apiClient, buildAuthHeaders } from '../client';

export const frameApi = {
	async create(payload: {
		boardId: string;
		posX: number;
		posY: number;
		width: number;
		height: number;
		title: string;
	}) {
		const response = await apiClient.post('/frames', payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async update(elementId: string, payload: Record<string, unknown>) {
		const response = await apiClient.put(`/frames/${elementId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

    async delete(elementId: string) {
		const response = await apiClient.delete(`/frames/${elementId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},
};
