package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;
import event.to.ai.backend.board.application.port.out.BoardEventRepositoryPort;
import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardEvent;
import event.to.ai.backend.board.domain.BoardId;
import event.to.ai.backend.board.domain.BoardTitle;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class BoardEventPersistenceAdapter implements BoardEventRepositoryPort {

    private final BoardRepository boardRepository;

    public BoardEventPersistenceAdapter(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Optional<Board> findById(BoardId boardId) {
        return boardRepository.findById(boardId.id()).map(this::toAggregate);
    }

    @Override
    public void save(Board board) {
        if (board.isDeleted()) {
            boardRepository.deleteById(board.getId().id());
            return;
        }

        BoardJpaEntity entity = boardRepository.findById(board.getId().id()).orElseGet(BoardJpaEntity::new);
        entity.setId(board.getId().id());
        entity.setTitle(board.getTitle().title());
        entity.setDescription(board.getDescription());
        entity.setOwnerId(board.getOwnerId());
        entity.setCreatedAt(board.getCreatedAt());
        entity.setUpdatedAt(board.getUpdatedAt());
        boardRepository.save(entity);
    }

    private Board toAggregate(BoardJpaEntity entity) {
        Instant createdAt = entity.getCreatedAt() == null
                ? Instant.now()
                : entity.getCreatedAt().toInstant(ZoneOffset.UTC);

        BoardEvent.BoardCreated created = new BoardEvent.BoardCreated(
                UUID.randomUUID(),
                createdAt,
                entity.getId().toString(),
                Map.of(),
                BoardId.valueOf(entity.getId()),
                entity.getOwnerId(),
                BoardTitle.valueOf(entity.getTitle()),
                entity.getDescription() == null ? "" : entity.getDescription()
        );

        return new Board(List.of(created));
    }
}