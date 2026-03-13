package event.to.ai.backend.board.application.usecase.query.output;

import event.to.ai.backend.board.application.port.out.BoardReadModel;
import tw.teddysoft.ezddd.cqrs.usecase.CqrsOutput;

public class GetBoardByIdOutput extends CqrsOutput<GetBoardByIdOutput> {

    private BoardReadModel board;

    public BoardReadModel getBoard() {
        return board;
    }

    public GetBoardByIdOutput setBoard(BoardReadModel board) {
        this.board = board;
        return this;
    }
}