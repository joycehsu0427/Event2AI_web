package event.to.ai.backend.board.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardReadModelEntity;
import org.springframework.stereotype.Component;

@Component
public class BoardReadModelMapper {

    public BoardReadModel toReadModel(BoardReadModelEntity entity) {
        return new BoardReadModel(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOwnerId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public BoardReadModelEntity toEntity(BoardReadModel model) {
        BoardReadModelEntity entity = new BoardReadModelEntity();
        entity.setId(model.id());
        entity.setTitle(model.title());
        entity.setDescription(model.description());
        entity.setOwnerId(model.ownerId());
        entity.setCreatedAt(model.createdAt());
        entity.setUpdatedAt(model.updatedAt());
        return entity;
    }
}
