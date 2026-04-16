import { apiClient, buildAuthHeaders } from './client';

export const commonApi = {
	async analysis(boardId: string) {
		const response = await apiClient.post('/analysis', { boardId: boardId }, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},
};