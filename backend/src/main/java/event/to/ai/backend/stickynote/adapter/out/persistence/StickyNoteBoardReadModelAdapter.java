package event.to.ai.backend.stickynote.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardReadModelEntity;
import event.to.ai.backend.board.adapter.out.persistence.BoardReadModelRepository;
import event.to.ai.backend.stickynote.application.port.out.BoardReadModelRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class StickyNoteBoardReadModelAdapter implements BoardReadModelRepositoryPort {

    private final BoardReadModelRepository boardReadModelRepository;

    @Autowired
    public StickyNoteBoardReadModelAdapter(BoardReadModelRepository boardReadModelRepository) {
        this.boardReadModelRepository = boardReadModelRepository;
    }

    @Override
    public Optional<BoardReadModelEntity> findById(UUID id) {
        return boardReadModelRepository.findById(id);
    }
}
