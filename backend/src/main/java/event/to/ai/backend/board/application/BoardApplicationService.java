package event.to.ai.backend.board.application;

import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;
import event.to.ai.backend.board.application.port.out.BoardEventRepositoryPort;
import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardId;
import event.to.ai.backend.board.domain.BoardTitle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoardApplicationService {

    private final BoardRepositoryPort boardRepositoryPort;
    private final BoardEventRepositoryPort boardEventRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public BoardApplicationService(BoardRepositoryPort boardRepositoryPort,
                                   BoardEventRepositoryPort boardEventRepositoryPort,
                                   UserRepositoryPort userRepositoryPort) {
        this.boardRepositoryPort = boardRepositoryPort;
        this.boardEventRepositoryPort = boardEventRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    public List<BoardDTO> getAllMyBoards(UUID userId) {
        return boardRepositoryPort.findAllByOwnerId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BoardDTO getBoardById(UUID id) {
        BoardJpaEntity boardJpaEntity = boardRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));
        return toDTO(boardJpaEntity);
    }

    @Transactional
    public BoardDTO createBoard(UUID actorUserId, CreateBoardRequest request) {
        userRepositoryPort.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + actorUserId));

        String title = normalizeTitle(request.getTitle());
        String description = normalizeDescription(request.getDescription());

        Board board = Board.create(BoardId.create(), BoardTitle.valueOf(title), description, actorUserId);
        boardEventRepositoryPort.save(board);
        return toDTO(board);
    }

    @Transactional
    public BoardDTO updateBoard(UUID id, UpdateBoardRequest request) {
        Board board = boardEventRepositoryPort.findById(BoardId.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        if (request.getTitle() != null) {
            board.rename(BoardTitle.valueOf(normalizeTitle(request.getTitle())));
        }

        if (request.getDescription() != null) {
            board.changeDescription(normalizeDescription(request.getDescription()));
        }

        boardEventRepositoryPort.save(board);
        return toDTO(board);
    }

    @Transactional
    public void deleteBoard(UUID id) {
        Board board = boardEventRepositoryPort.findById(BoardId.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        board.delete();
        boardEventRepositoryPort.save(board);
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

    private BoardDTO toDTO(BoardJpaEntity boardJpaEntity) {
        return new BoardDTO(
                boardJpaEntity.getId(),
                boardJpaEntity.getTitle(),
                boardJpaEntity.getDescription(),
                boardJpaEntity.getOwnerId(),
                boardJpaEntity.getCreatedAt(),
                boardJpaEntity.getUpdatedAt()
        );
    }

    private BoardDTO toDTO(Board board) {
        return new BoardDTO(
                board.getId().id(),
                board.getTitle().title(),
                board.getDescription(),
                board.getOwnerId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}