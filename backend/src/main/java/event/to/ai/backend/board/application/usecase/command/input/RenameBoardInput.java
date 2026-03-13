package event.to.ai.backend.board.application.usecase.command.input;

import tw.teddysoft.ezddd.usecase.port.in.interactor.VersionedInput;

import java.util.UUID;

public class RenameBoardInput implements VersionedInput {

    private UUID actorUserId;
    private UUID boardId;
    private String title;
    private long version = -1L;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }
}