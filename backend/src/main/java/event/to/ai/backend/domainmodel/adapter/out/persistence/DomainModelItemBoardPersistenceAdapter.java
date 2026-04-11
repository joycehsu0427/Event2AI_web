package event.to.ai.backend.domainmodel.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.BoardRepository;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.domainmodel.application.port.out.BoardRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class DomainModelItemBoardPersistenceAdapter implements BoardRepositoryPort {

    private final BoardRepository boardRepository;

    @Autowired
    public DomainModelItemBoardPersistenceAdapter(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Optional<Board> findById(UUID id) {
        return boardRepository.findById(id);
    }
}
