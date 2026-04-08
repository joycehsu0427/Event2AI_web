package event.to.ai.backend.websocket;

public enum BoardRealtimeEventType {
    STICKY_NOTE_CREATED("stickyNote.created"),
    STICKY_NOTE_UPDATED("stickyNote.updated"),
    STICKY_NOTE_DELETED("stickyNote.deleted");

    private final String value;

    BoardRealtimeEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
