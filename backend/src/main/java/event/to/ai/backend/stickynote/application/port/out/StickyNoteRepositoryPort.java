package event.to.ai.backend.stickynote.application.port.out;

import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StickyNoteRepositoryPort {

    List<StickyNote> findAll();

    Optional<StickyNote> findById(UUID id);

    List<StickyNote> findByBoardId(UUID boardId);

    List<StickyNote> findByBoardIdAndColor(UUID boardId, String color);

    List<StickyNote> findByColor(String color);

    StickyNote save(StickyNote stickyNote);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
