package event.to.ai.backend.board.application.usecase.query.projection;

import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.usecase.query.projection.input.GetMyBoardsProjectionInput;
import tw.teddysoft.ezddd.cqrs.usecase.query.Projection;

import java.util.List;

public interface MyBoardsProjection extends Projection<GetMyBoardsProjectionInput, List<BoardReadModel>> {
}