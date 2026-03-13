package event.to.ai.backend.board.adapter.out.persistence.projection;

import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.port.out.BoardReadModelQueryPort;
import event.to.ai.backend.board.application.usecase.query.projection.BoardByIdProjection;
import event.to.ai.backend.board.application.usecase.query.projection.input.GetBoardByIdProjectionInput;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BoardByIdProjectionAdapter implements BoardByIdProjection {

    private final BoardReadModelQueryPort boardReadModelQueryPort;

    public BoardByIdProjectionAdapter(BoardReadModelQueryPort boardReadModelQueryPort) {
        this.boardReadModelQueryPort = boardReadModelQueryPort;
    }

    @Override
    public Optional<BoardReadModel> query(GetBoardByIdProjectionInput input) {
        return boardReadModelQueryPort.findById(input.getBoardId());
    }
}
