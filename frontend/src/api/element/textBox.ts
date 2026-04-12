import { apiClient, buildAuthHeaders } from '../client';

export const textBoxApi = {
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
		const response = await apiClient.post('/text_boxes', payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async update(elementId: string, payload: Record<string, unknown>) {
		const response = await apiClient.put(`/text_boxes/${elementId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async delete(elementId: string) {
		const response = await apiClient.delete(`/text_boxes/${elementId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},
};
