package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.application.port.out.BoardContentRepositoryPort;
import event.to.ai.backend.connector.adapter.out.persistence.ConnectorRepository;
import event.to.ai.backend.domainmodel.adapter.out.persistence.DomainModelItemRepository;
import event.to.ai.backend.frame.adapter.out.persistence.FrameRepository;
import event.to.ai.backend.stickynote.adapter.out.persistence.StickyNoteRepository;
import event.to.ai.backend.textbox.adapter.out.persistence.TextBoxesRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BoardContentPersistenceAdapter implements BoardContentRepositoryPort {

    private final ConnectorRepository connectorRepository;
    private final StickyNoteRepository stickyNoteRepository;
    private final TextBoxesRepository textBoxesRepository;
    private final DomainModelItemRepository domainModelItemRepository;
    private final FrameRepository frameRepository;

    public BoardContentPersistenceAdapter(ConnectorRepository connectorRepository,
                                          StickyNoteRepository stickyNoteRepository,
                                          TextBoxesRepository textBoxesRepository,
                                          DomainModelItemRepository domainModelItemRepository,
                                          FrameRepository frameRepository) {
        this.connectorRepository = connectorRepository;
        this.stickyNoteRepository = stickyNoteRepository;
        this.textBoxesRepository = textBoxesRepository;
        this.domainModelItemRepository = domainModelItemRepository;
        this.frameRepository = frameRepository;
    }

    @Override
    public void deleteAllByBoardId(UUID boardId) {
        connectorRepository.deleteAllByBoardId(boardId);
        stickyNoteRepository.deleteAllByBoardId(boardId);
        textBoxesRepository.deleteAllByBoardId(boardId);
        domainModelItemRepository.deleteAllByBoardId(boardId);
        frameRepository.deleteAllByBoardId(boardId);
    }
}
