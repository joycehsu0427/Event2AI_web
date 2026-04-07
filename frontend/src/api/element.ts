import { apiClient, buildAuthHeaders } from './client';

export const elementApi = {
	async createStickyNote(payload: {
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

	async createTextBox(payload: {
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

	async createFrame(payload: {
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

	async updateTextBox(elementId: string, payload: Record<string, unknown>) {
		const response = await apiClient.put(`/text_boxes/${elementId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async updateStickyNote(elementId: string, payload: Record<string, unknown>) {
		const response = await apiClient.put(`/sticky-notes/${elementId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async updateFrame(elementId: string, payload: Record<string, unknown>) {
		const response = await apiClient.put(`/frames/${elementId}`, payload, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async deleteStickyNote(elementId: string) {
		const response = await apiClient.delete(`/sticky-notes/${elementId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},

	async deleteTextBox(elementId: string) {
		const response = await apiClient.delete(`/text_boxes/${elementId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},
};