package event.to.ai.backend.board.application.port.out;

import tw.teddysoft.ezddd.cqrs.usecase.query.Archive;

import java.util.UUID;

public interface BoardReadModelArchive extends Archive<BoardReadModel, UUID> {
}