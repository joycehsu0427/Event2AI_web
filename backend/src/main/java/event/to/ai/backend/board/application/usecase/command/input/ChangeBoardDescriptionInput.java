package event.to.ai.backend.board.application.usecase.command.input;

import tw.teddysoft.ezddd.usecase.port.in.interactor.VersionedInput;

import java.util.UUID;

public class ChangeBoardDescriptionInput implements VersionedInput {

    private UUID actorUserId;
    private UUID boardId;
    private String description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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