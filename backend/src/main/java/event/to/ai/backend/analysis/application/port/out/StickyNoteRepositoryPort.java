package event.to.ai.backend.analysis.application.port.out;

import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;

import java.util.List;
import java.util.UUID;

public interface StickyNoteRepositoryPort {
    List<StickyNote> findByBoardId(UUID boardId);
}
