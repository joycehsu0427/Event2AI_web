package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardEventStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardEventStoreRepository extends JpaRepository<BoardEventStoreEntity, Long> {

    List<BoardEventStoreEntity> findAllByStreamNameOrderByVersionAsc(String streamName);

    Optional<BoardEventStoreEntity> findTopByStreamNameOrderByVersionDesc(String streamName);
}
