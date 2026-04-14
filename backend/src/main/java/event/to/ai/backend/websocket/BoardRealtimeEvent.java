package event.to.ai.backend.websocket;

import java.util.UUID;

// STOMP board topic 使用的統一事件 envelope。
public class BoardRealtimeEvent {

    private BoardRealtimeEventType type;
    private UUID boardId;
    private UUID userId;
    // payload 直接沿用既有 DTO，避免維護第二套即時同步模型。
    private Object payload;

    public BoardRealtimeEvent() {
    }

    public BoardRealtimeEvent(BoardRealtimeEventType type, UUID userId, UUID boardId, Object payload) {
        this.type = type;
        this.boardId = boardId;
        this.userId = userId;
        this.payload = payload;
    }

    public BoardRealtimeEventType getType() {
        return type;
    }

    public void setType(BoardRealtimeEventType type) {
        this.type = type;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
