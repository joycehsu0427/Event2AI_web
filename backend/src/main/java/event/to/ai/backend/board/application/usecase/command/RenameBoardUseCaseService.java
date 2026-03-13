package event.to.ai.backend.board.application.usecase.command;

import event.to.ai.backend.board.application.port.out.BoardEventRepositoryPort;
import event.to.ai.backend.board.application.usecase.command.input.RenameBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardId;
import event.to.ai.backend.board.domain.BoardTitle;
import org.springframework.stereotype.Service;

@Service
public class RenameBoardUseCaseService implements RenameBoardUseCase {

    private final BoardEventRepositoryPort boardEventRepositoryPort;

    public RenameBoardUseCaseService(BoardEventRepositoryPort boardEventRepositoryPort) {
        this.boardEventRepositoryPort = boardEventRepositoryPort;
    }

    @Override
    public BoardCommandOutput execute(RenameBoardInput input) {
        Board board = loadBoard(input.getBoardId());
        validateVersion(input, board);
        board.rename(BoardTitle.valueOf(normalizeTitle(input.getTitle())));
        boardEventRepositoryPort.save(board);
        return new BoardCommandOutput()
                .setBoardId(board.getId().id())
                .setVersion(board.getVersion())
                .succeed();
    }

    private Board loadBoard(java.util.UUID boardId) {
        return boardEventRepositoryPort.findById(BoardId.valueOf(boardId))
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardId));
    }

    private void validateVersion(RenameBoardInput input, Board board) {
        if (input.getVersion() >= 0 && input.getVersion() != board.getVersion()) {
            throw new RuntimeException("Board version mismatch");
        }
    }

    private String normalizeTitle(String rawTitle) {
        String title = rawTitle.trim();
        if (title.isEmpty()) {
            throw new RuntimeException("Board title cannot be blank");
        }
        return title;
    }
}