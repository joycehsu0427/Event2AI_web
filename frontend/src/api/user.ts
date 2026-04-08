import { apiClient, buildAuthHeaders } from './client';

export const userApi = {
	async getById(userId: string) {
		const response = await apiClient.get(`/users/${userId}`, {
			headers: buildAuthHeaders(),
		});
		return response.data;
	},
};