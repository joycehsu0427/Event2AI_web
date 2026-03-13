package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardReadModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface BoardReadModelRepository extends JpaRepository<BoardReadModelEntity, UUID> {

    List<BoardReadModelEntity> findAllByOwnerId(UUID ownerId);
}
