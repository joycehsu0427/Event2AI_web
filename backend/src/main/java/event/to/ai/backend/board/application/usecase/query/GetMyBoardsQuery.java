package event.to.ai.backend.board.application.usecase.query;

import event.to.ai.backend.board.application.usecase.query.input.GetMyBoardsInput;
import event.to.ai.backend.board.application.usecase.query.output.GetMyBoardsOutput;
import tw.teddysoft.ezddd.cqrs.usecase.query.Query;

public interface GetMyBoardsQuery extends Query<GetMyBoardsInput, GetMyBoardsOutput> {
}