import { apiClient } from './client';

export interface AuthLoginResponse {
	accessToken: string;
	user?: unknown;
}

export const authApi = {
	async login(username: string, password: string) {
		const response = await apiClient.post<AuthLoginResponse>('/auth/login', {
			username,
			password,
		});
		return response.data;
	},

	async register(username: string, email: string, password: string) {
		const response = await apiClient.post('/auth/register', {
			username,
			email,
			password,
		});
		return response.data;
	},
};