package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardReadModelEntity;
import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.port.out.BoardReadModelArchive;
import event.to.ai.backend.board.application.port.out.BoardReadModelMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BoardReadModelArchiveAdapter implements BoardReadModelArchive {

    private final BoardReadModelRepository boardReadModelRepository;
    private final BoardReadModelMapper boardReadModelMapper;

    public BoardReadModelArchiveAdapter(BoardReadModelRepository boardReadModelRepository,
                                        BoardReadModelMapper boardReadModelMapper) {
        this.boardReadModelRepository = boardReadModelRepository;
        this.boardReadModelMapper = boardReadModelMapper;
    }

    @Override
    public java.util.Optional<BoardReadModel> findById(UUID id) {
        return boardReadModelRepository.findById(id).map(boardReadModelMapper::toReadModel);
    }

    @Override
    public void save(BoardReadModel model) {
        boardReadModelRepository.save(boardReadModelMapper.toEntity(model));
    }

    @Override
    public void delete(BoardReadModel model) {
        boardReadModelRepository.deleteById(model.id());
    }
}
