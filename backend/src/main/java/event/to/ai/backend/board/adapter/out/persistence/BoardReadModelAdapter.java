package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardReadModelEntity;
import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.port.out.BoardReadModelQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BoardReadModelAdapter implements BoardReadModelQueryPort {

    private final BoardReadModelRepository boardReadModelRepository;

    public BoardReadModelAdapter(BoardReadModelRepository boardReadModelRepository) {
        this.boardReadModelRepository = boardReadModelRepository;
    }

    @Override
    public List<BoardReadModel> findAllByOwnerId(UUID ownerId) {
        return boardReadModelRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(this::toReadModel)
                .toList();
    }

    @Override
    public Optional<BoardReadModel> findById(UUID id) {
        return boardReadModelRepository.findById(id).map(this::toReadModel);
    }

    private BoardReadModel toReadModel(BoardReadModelEntity entity) {
        return new BoardReadModel(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOwnerId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
