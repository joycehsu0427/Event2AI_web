package event.to.ai.backend.board.application.usecase.query;

import event.to.ai.backend.board.application.usecase.query.input.GetBoardByIdInput;
import event.to.ai.backend.board.application.usecase.query.output.GetBoardByIdOutput;
import tw.teddysoft.ezddd.cqrs.usecase.query.Query;

public interface GetBoardByIdQuery extends Query<GetBoardByIdInput, GetBoardByIdOutput> {
}