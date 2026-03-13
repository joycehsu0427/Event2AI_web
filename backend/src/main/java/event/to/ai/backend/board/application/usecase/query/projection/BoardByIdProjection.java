package event.to.ai.backend.board.application.usecase.query.projection;

import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.usecase.query.projection.input.GetBoardByIdProjectionInput;
import tw.teddysoft.ezddd.cqrs.usecase.query.Projection;

import java.util.Optional;

public interface BoardByIdProjection extends Projection<GetBoardByIdProjectionInput, Optional<BoardReadModel>> {
}