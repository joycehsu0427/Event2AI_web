package event.to.ai.backend.board.application.usecase.command;

import event.to.ai.backend.board.application.port.out.BoardEventRepositoryPort;
import event.to.ai.backend.board.application.usecase.command.input.DeleteBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardId;
import org.springframework.stereotype.Service;

@Service
public class DeleteBoardUseCaseService implements DeleteBoardUseCase {

    private final BoardEventRepositoryPort boardEventRepositoryPort;

    public DeleteBoardUseCaseService(BoardEventRepositoryPort boardEventRepositoryPort) {
        this.boardEventRepositoryPort = boardEventRepositoryPort;
    }

    @Override
    public BoardCommandOutput execute(DeleteBoardInput input) {
        Board board = boardEventRepositoryPort.findById(BoardId.valueOf(input.getBoardId()))
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + input.getBoardId()));
        if (input.getVersion() >= 0 && input.getVersion() != board.getVersion()) {
            throw new RuntimeException("Board version mismatch");
        }
        board.delete();
        boardEventRepositoryPort.save(board);
        return new BoardCommandOutput()
                .setBoardId(board.getId().id())
                .setVersion(board.getVersion())
                .succeed();
    }
}