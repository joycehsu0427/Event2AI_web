package event.to.ai.backend.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BoardRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public BoardRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 所有 board realtime 訊息都統一送到同一個 topic，由前端用 type 分流。
    public void publish(BoardRealtimeEventType type, UUID boardId, Object payload) {
        messagingTemplate.convertAndSend(topicFor(boardId), new BoardRealtimeEvent(type, boardId, payload));
    }

    public String topicFor(UUID boardId) {
        return "/topic/boards/" + boardId + "/events";
    }
}
