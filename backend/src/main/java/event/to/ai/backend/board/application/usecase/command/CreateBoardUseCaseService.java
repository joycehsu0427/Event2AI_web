package event.to.ai.backend.board.application.usecase.command;

import event.to.ai.backend.board.application.port.out.BoardEventRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.application.usecase.command.input.CreateBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardId;
import event.to.ai.backend.board.domain.BoardTitle;
import org.springframework.stereotype.Service;

@Service
public class CreateBoardUseCaseService implements CreateBoardUseCase {

    private final BoardEventRepositoryPort boardEventRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateBoardUseCaseService(BoardEventRepositoryPort boardEventRepositoryPort,
                                     UserRepositoryPort userRepositoryPort) {
        this.boardEventRepositoryPort = boardEventRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public BoardCommandOutput execute(CreateBoardInput input) {
        requireExistingUser(input.getActorUserId());
        String title = normalizeTitle(input.getTitle());
        String description = normalizeDescription(input.getDescription());

        Board board = Board.create(BoardId.create(), BoardTitle.valueOf(title), description, input.getActorUserId());
        boardEventRepositoryPort.save(board);

        return new BoardCommandOutput()
                .setBoardId(board.getId().id())
                .setVersion(board.getVersion())
                .succeed();
    }

    private void requireExistingUser(java.util.UUID userId) {
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private String normalizeTitle(String rawTitle) {
        String title = rawTitle.trim();
        if (title.isEmpty()) {
            throw new RuntimeException("Board title cannot be blank");
        }
        return title;
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description;
    }
}