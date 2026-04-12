package event.to.ai.backend.websocket;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BoardRealtimeEventType {
    STICKY_NOTE_CREATED("stickyNote.created"),
    STICKY_NOTE_UPDATED("stickyNote.updated"),
    STICKY_NOTE_DELETED("stickyNote.deleted"),
    TEXT_BOX_CREATED("textBox.created"),
    TEXT_BOX_UPDATED("textBox.updated"),
    TEXT_BOX_DELETED("textBox.deleted"),
    FRAME_CREATED("frame.created"),
    FRAME_UPDATED("frame.updated"),
    FRAME_DELETED("frame.deleted"),
    DOMAIN_MODEL_ITEM_CREATED("domainModelItem.created"),
    DOMAIN_MODEL_ITEM_UPDATED("domainModelItem.updated"),
    DOMAIN_MODEL_ITEM_DELETED("domainModelItem.deleted");

    private final String value;

    BoardRealtimeEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
