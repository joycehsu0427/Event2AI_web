package event.to.ai.backend.board.application.usecase.query.input;

import tw.teddysoft.ezddd.usecase.port.in.interactor.Input;

import java.util.UUID;

public class GetBoardByIdInput implements Input {

    private UUID boardId;
    private UUID actorUserId;

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }
}