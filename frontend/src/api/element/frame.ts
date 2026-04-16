import { apiClient, buildAuthHeaders } from '../client';

export interface EventStormingTemplateRequest {
	boardId: string;
	posX: number;
	posY: number;
}

export interface StickyNoteDTO {
	id: string;
	boardId?: string;
	posX: number;
	posY: number;
	geoX: number;
	geoY: number;
	frameId?: string | null;
	description: string;
	color: string;
	fontColor?: string;
	fontSize?: number | string;
	tag?: string;
}

export interface TextBoxesDTO {
	id: string;
	boardId?: string;
	posX: number;
	posY: number;
	geoX: number;
	geoY: number;
	frameId?: string | null;
	description: string;
	fontColor?: string;
	fontSize?: number | string;
	tag?: string;
}

export interface FrameDTO {
	id: string;
	boardId?: string;
	posX: number;
	posY: number;
	width: number;
	height: number;
	title: string;
}

export interface DomainModelItemDTO {
	id: string;
	boardId?: string;
	posX: number;
	posY: number;
	width: number;
	height: number;
	name: string;
	type: string;
	description?: string;
	attributes?: Array<{
		name: string;
		dataType?: string;
		constraint?: string;
		displayOrder?: number;
	}>;
}

export interface EventStormingTemplateResponse {
	boardId: string;
	stickyNotes: StickyNoteDTO[];
	textBoxes: TextBoxesDTO[];
	frames: FrameDTO[];
	domainModelItems: DomainModelItemDTO[];
}

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

	async createEventStormingTemplate(payload: EventStormingTemplateRequest) {
		const response = await apiClient.post('/frames/event_storming_template', payload, {
			headers: buildAuthHeaders(),
		});
		return response.data as EventStormingTemplateResponse;
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
