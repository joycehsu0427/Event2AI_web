package event.to.ai.backend.board.application;

import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoardApplicationService {

    private final BoardRepositoryPort boardRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Autowired
    public BoardApplicationService(BoardRepositoryPort boardRepositoryPort,
                                   UserRepositoryPort userRepositoryPort) {
        this.boardRepositoryPort = boardRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    public List<BoardDTO> getAllBoards() {
        return boardRepositoryPort.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BoardDTO getBoardById(UUID id) {
        Board board = boardRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));
        return toDTO(board);
    }

    @Transactional
    public BoardDTO createBoard(Long actorUserId, CreateBoardRequest request) {
        userRepositoryPort.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + actorUserId));

        String title = request.getTitle().trim();
        if (title.isEmpty()) {
            throw new RuntimeException("Board title cannot be blank");
        }

        Board board = new Board(title, request.getDescription());
        board.setOwnerId(actorUserId);
        Board savedBoard = boardRepositoryPort.save(board);
        return toDTO(savedBoard);
    }

    @Transactional
    public BoardDTO updateBoard(UUID id, UpdateBoardRequest request) {
        Board board = boardRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new RuntimeException("Board title cannot be blank");
            }
            board.setTitle(title);
        }

        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }

        Board updatedBoard = boardRepositoryPort.save(board);
        return toDTO(updatedBoard);
    }

    @Transactional
    public void deleteBoard(UUID id) {
        if (!boardRepositoryPort.existsById(id)) {
            throw new RuntimeException("Board not found with id: " + id);
        }
        boardRepositoryPort.deleteById(id);
    }

    private BoardDTO toDTO(Board board) {
        return new BoardDTO(
                board.getId(),
                board.getTitle(),
                board.getDescription(),
                board.getOwnerId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
