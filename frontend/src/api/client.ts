import axios from 'axios';

export const apiClient = axios.create({
	baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
});

export function buildAuthHeaders() {
	const token = localStorage.getItem('token');
	return token ? { Authorization: `Bearer ${token}` } : undefined;
}