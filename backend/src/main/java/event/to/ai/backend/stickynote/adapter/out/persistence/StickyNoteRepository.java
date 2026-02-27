package event.to.ai.backend.stickynote.adapter.out.persistence;

import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StickyNoteRepository extends JpaRepository<StickyNote, UUID> {

    List<StickyNote> findByBoardId(UUID boardId);

    List<StickyNote> findByBoardIdAndColor(UUID boardId, String color);

    List<StickyNote> findByColor(String color);
}
