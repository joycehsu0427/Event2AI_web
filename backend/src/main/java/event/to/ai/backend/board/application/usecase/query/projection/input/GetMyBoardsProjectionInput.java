package event.to.ai.backend.board.application.usecase.query.projection.input;

import tw.teddysoft.ezddd.cqrs.usecase.query.ProjectionInput;

import java.util.UUID;

public class GetMyBoardsProjectionInput implements ProjectionInput {

    private UUID ownerId;

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
}