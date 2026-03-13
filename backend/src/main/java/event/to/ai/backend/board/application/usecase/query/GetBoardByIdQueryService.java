package event.to.ai.backend.board.application.usecase.query;

import event.to.ai.backend.board.application.usecase.query.input.GetBoardByIdInput;
import event.to.ai.backend.board.application.usecase.query.output.GetBoardByIdOutput;
import event.to.ai.backend.board.application.usecase.query.projection.BoardByIdProjection;
import event.to.ai.backend.board.application.usecase.query.projection.input.GetBoardByIdProjectionInput;
import org.springframework.stereotype.Service;

@Service
public class GetBoardByIdQueryService implements GetBoardByIdQuery {

    private final BoardByIdProjection projection;

    public GetBoardByIdQueryService(BoardByIdProjection projection) {
        this.projection = projection;
    }

    @Override
    public GetBoardByIdOutput execute(GetBoardByIdInput input) {
        GetBoardByIdProjectionInput projectionInput = new GetBoardByIdProjectionInput();
        projectionInput.setBoardId(input.getBoardId());

        return projection.query(projectionInput)
                .filter(model -> input.getActorUserId() == null || model.ownerId().equals(input.getActorUserId()))
                .map(model -> new GetBoardByIdOutput().setBoard(model).succeed())
                .orElseGet(() -> new GetBoardByIdOutput().fail().setMessage("Board not found"));
    }
}