package event.to.ai.backend.board.application.usecase.query.output;

import event.to.ai.backend.board.application.port.out.BoardReadModel;
import tw.teddysoft.ezddd.cqrs.usecase.CqrsOutput;

import java.util.List;

public class GetMyBoardsOutput extends CqrsOutput<GetMyBoardsOutput> {

    private List<BoardReadModel> boards;

    public List<BoardReadModel> getBoards() {
        return boards;
    }

    public GetMyBoardsOutput setBoards(List<BoardReadModel> boards) {
        this.boards = boards;
        return this;
    }
}