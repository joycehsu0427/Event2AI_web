import { apiClient, buildAuthHeaders } from './client';

export const commonApi = {
	async analysis(boardId: string) {
		await apiClient.post('/analysis', { boardId: boardId }, {
			headers: buildAuthHeaders(),
		});
	},
};