package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BoardPersistenceAdapter implements BoardRepositoryPort {

    private final BoardRepository boardRepository;

    @Autowired
    public BoardPersistenceAdapter(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public List<BoardJpaEntity> findAll() {
        return boardRepository.findAll();
    }

    @Override
    public List<BoardJpaEntity> findAllByOwnerId(UUID ownerId) {
        return boardRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public Optional<BoardJpaEntity> findById(UUID id) {
        return boardRepository.findById(id);
    }

    @Override
    public BoardJpaEntity save(BoardJpaEntity boardJpaEntity) {
        return boardRepository.save(boardJpaEntity);
    }

    @Override
    public boolean existsById(UUID id) {
        return boardRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        boardRepository.deleteById(id);
    }
}
