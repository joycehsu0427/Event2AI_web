package event.to.ai.backend.frame.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.BoardRepository;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.frame.application.port.out.BoardRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FrameBoardPersistenceAdapter implements BoardRepositoryPort {

    private final BoardRepository boardRepository;

    @Autowired
    public FrameBoardPersistenceAdapter(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Optional<Board> findById(UUID id) {
        return boardRepository.findById(id);
    }
}
