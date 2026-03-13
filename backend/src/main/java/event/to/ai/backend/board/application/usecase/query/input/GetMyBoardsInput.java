package event.to.ai.backend.board.application.usecase.query.input;

import tw.teddysoft.ezddd.usecase.port.in.interactor.Input;

import java.util.UUID;

public class GetMyBoardsInput implements Input {

    private UUID actorUserId;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }
}