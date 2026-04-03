package event.to.ai.backend.analysis.adapter.out.persistence;

import event.to.ai.backend.analysis.application.port.out.StickyNoteRepositoryPort;
import event.to.ai.backend.stickynote.adapter.out.persistence.StickyNoteRepository;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class StickyNoteAnalysisPersistenceAdapter implements StickyNoteRepositoryPort {

    private final StickyNoteRepository stickyNoteRepository;

    @Autowired
    public StickyNoteAnalysisPersistenceAdapter(StickyNoteRepository stickyNoteRepository) {
        this.stickyNoteRepository = stickyNoteRepository;
    }

    @Override
    public List<StickyNote> findByBoardId(UUID boardId) {
        return stickyNoteRepository.findByBoardId(boardId);
    }
}
