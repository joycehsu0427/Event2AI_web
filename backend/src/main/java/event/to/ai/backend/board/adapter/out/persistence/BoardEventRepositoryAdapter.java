package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.projector.BoardProjector;
import event.to.ai.backend.board.application.port.out.BoardEventRepositoryPort;
import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardEvent;
import event.to.ai.backend.board.domain.BoardId;
import org.springframework.stereotype.Component;
import tw.teddysoft.ezddd.usecase.port.out.repository.impl.RepositoryPeer;
import tw.teddysoft.ezddd.usecase.port.out.repository.impl.es.EsRepository;
import tw.teddysoft.ezddd.usecase.port.out.repository.impl.es.EventStoreData;

import java.util.List;
import java.util.Optional;

@Component
public class BoardEventRepositoryAdapter implements BoardEventRepositoryPort {

    private final EsRepository<Board, BoardId> esRepository;
    private final BoardProjector boardProjector;

    public BoardEventRepositoryAdapter(RepositoryPeer<EventStoreData, String> repositoryPeer,
                                       BoardProjector boardProjector) {
        this.esRepository = new EsRepository<>(repositoryPeer, Board.class, "board");
        this.boardProjector = boardProjector;
    }

    @Override
    public Optional<Board> findById(BoardId boardId) {
        return esRepository.findById(boardId);
    }

    @Override
    public void save(Board board) {
        List<BoardEvent> newEvents = List.copyOf(board.getDomainEvents());
        if (newEvents.isEmpty()) {
            return;
        }

        if (board.isDeleted()) {
            esRepository.delete(board);
        } else {
            esRepository.save(board);
        }

        boardProjector.project(newEvents);
    }
}