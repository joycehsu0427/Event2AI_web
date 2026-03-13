package event.to.ai.backend.board.adapter.out.persistence.projection;

import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.port.out.BoardReadModelQueryPort;
import event.to.ai.backend.board.application.usecase.query.projection.MyBoardsProjection;
import event.to.ai.backend.board.application.usecase.query.projection.input.GetMyBoardsProjectionInput;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyBoardsProjectionAdapter implements MyBoardsProjection {

    private final BoardReadModelQueryPort boardReadModelQueryPort;

    public MyBoardsProjectionAdapter(BoardReadModelQueryPort boardReadModelQueryPort) {
        this.boardReadModelQueryPort = boardReadModelQueryPort;
    }

    @Override
    public List<BoardReadModel> query(GetMyBoardsProjectionInput input) {
        return boardReadModelQueryPort.findAllByOwnerId(input.getOwnerId());
    }
}
