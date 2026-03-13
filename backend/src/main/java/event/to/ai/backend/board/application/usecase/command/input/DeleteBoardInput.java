package event.to.ai.backend.board.application.usecase.command.input;

import tw.teddysoft.ezddd.usecase.port.in.interactor.VersionedInput;

import java.util.UUID;

public class DeleteBoardInput implements VersionedInput {

    private UUID actorUserId;
    private UUID boardId;
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

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }
}