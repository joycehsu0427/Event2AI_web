package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardJpaEntity, UUID> {

    List<BoardJpaEntity> findAllByOwnerId(UUID ownerId);
}
