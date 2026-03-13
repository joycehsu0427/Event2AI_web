package event.to.ai.backend.board.adapter.out.persistence.projector;

import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.port.out.BoardReadModelArchive;
import event.to.ai.backend.board.domain.BoardEvent;
import org.springframework.stereotype.Component;
import tw.teddysoft.ezddd.cqrs.usecase.query.Projector;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class BoardProjector implements Projector {

    private final BoardReadModelArchive archive;

    public BoardProjector(BoardReadModelArchive archive) {
        this.archive = archive;
    }

    public void project(List<BoardEvent> boardEvents) {
        for (BoardEvent event : boardEvents) {
            switch (event) {
                case BoardEvent.BoardCreated e -> whenCreated(e);
                case BoardEvent.BoardRenamed e -> whenRenamed(e);
                case BoardEvent.BoardDescriptionChanged e -> whenDescriptionChanged(e);
                case BoardEvent.BoardDeleted e -> whenDeleted(e);
            }
        }
    }

    private void whenCreated(BoardEvent.BoardCreated event) {
        LocalDateTime occurredAt = LocalDateTime.ofInstant(event.occurredOn(), ZoneOffset.UTC);
        BoardReadModel model = new BoardReadModel(
                event.boardId().id(),
                event.title().title(),
                event.description(),
                event.ownerId(),
                occurredAt,
                occurredAt
        );
        archive.save(model);
    }

    private void whenRenamed(BoardEvent.BoardRenamed event) {
        BoardReadModel current = archive.findById(event.boardId().id())
                .orElseThrow(() -> new RuntimeException("Board read model not found: " + event.boardId()));
        archive.save(new BoardReadModel(
                current.id(),
                event.title().title(),
                current.description(),
                current.ownerId(),
                current.createdAt(),
                LocalDateTime.ofInstant(event.occurredOn(), ZoneOffset.UTC)
        ));
    }

    private void whenDescriptionChanged(BoardEvent.BoardDescriptionChanged event) {
        BoardReadModel current = archive.findById(event.boardId().id())
                .orElseThrow(() -> new RuntimeException("Board read model not found: " + event.boardId()));
        archive.save(new BoardReadModel(
                current.id(),
                current.title(),
                event.description(),
                current.ownerId(),
                current.createdAt(),
                LocalDateTime.ofInstant(event.occurredOn(), ZoneOffset.UTC)
        ));
    }

    private void whenDeleted(BoardEvent.BoardDeleted event) {
        archive.findById(event.boardId().id()).ifPresent(archive::delete);
    }
}