package event.to.ai.backend.board.application;

import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;
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

        String title = request.getTitle().trim();
        if (title.isEmpty()) {
            throw new RuntimeException("Board title cannot be blank");
        }

        BoardJpaEntity boardJpaEntity = new BoardJpaEntity(title, request.getDescription());
        boardJpaEntity.setOwnerId(actorUserId);
        BoardJpaEntity savedBoardJpaEntity = boardRepositoryPort.save(boardJpaEntity);
        return toDTO(savedBoardJpaEntity);
    }

    @Transactional
    public BoardDTO updateBoard(UUID id, UpdateBoardRequest request) {
        BoardJpaEntity boardJpaEntity = boardRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new RuntimeException("Board title cannot be blank");
            }
            boardJpaEntity.setTitle(title);
        }

        if (request.getDescription() != null) {
            boardJpaEntity.setDescription(request.getDescription());
        }

        BoardJpaEntity updatedBoardJpaEntity = boardRepositoryPort.save(boardJpaEntity);
        return toDTO(updatedBoardJpaEntity);
    }

    @Transactional
    public void deleteBoard(UUID id) {
        if (!boardRepositoryPort.existsById(id)) {
            throw new RuntimeException("Board not found with id: " + id);
        }
        boardRepositoryPort.deleteById(id);
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
}
