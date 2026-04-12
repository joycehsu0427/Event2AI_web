import { apiClient, buildAuthHeaders } from '../client';

export const stickyNoteApi = {
	async create(payload: {
		boardId: string;
		posX: number;
		posY: number;
		geoX: number;
		geoY: number;
		description: string;
		color: string;
		tag: string;
		fontColor: string;
		fontSize: number;
	}) {
		const response = await apiClient.post('/sticky-notes', payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async update(elementId: string, payload: Record<string, unknown>) {
		const response = await apiClient.put(`/sticky-notes/${elementId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async delete(elementId: string) {
		const response = await apiClient.delete(`/sticky-notes/${elementId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},
};
