package event.to.ai.backend.board.application.usecase.query.projection.input;

import tw.teddysoft.ezddd.cqrs.usecase.query.ProjectionInput;

import java.util.UUID;

public class GetBoardByIdProjectionInput implements ProjectionInput {

    private UUID boardId;

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }
}