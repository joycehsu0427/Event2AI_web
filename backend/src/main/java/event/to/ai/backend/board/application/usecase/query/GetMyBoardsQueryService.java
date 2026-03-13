package event.to.ai.backend.board.application.usecase.query;

import event.to.ai.backend.board.application.usecase.query.input.GetMyBoardsInput;
import event.to.ai.backend.board.application.usecase.query.output.GetMyBoardsOutput;
import event.to.ai.backend.board.application.usecase.query.projection.MyBoardsProjection;
import event.to.ai.backend.board.application.usecase.query.projection.input.GetMyBoardsProjectionInput;
import org.springframework.stereotype.Service;

@Service
public class GetMyBoardsQueryService implements GetMyBoardsQuery {

    private final MyBoardsProjection projection;

    public GetMyBoardsQueryService(MyBoardsProjection projection) {
        this.projection = projection;
    }

    @Override
    public GetMyBoardsOutput execute(GetMyBoardsInput input) {
        GetMyBoardsProjectionInput projectionInput = new GetMyBoardsProjectionInput();
        projectionInput.setOwnerId(input.getActorUserId());

        return new GetMyBoardsOutput()
                .setBoards(projection.query(projectionInput))
                .succeed();
    }
}