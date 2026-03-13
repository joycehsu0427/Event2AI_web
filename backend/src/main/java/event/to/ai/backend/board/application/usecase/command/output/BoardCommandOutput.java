package event.to.ai.backend.board.application.usecase.command.output;

import tw.teddysoft.ezddd.cqrs.usecase.CqrsOutput;

import java.util.UUID;

public class BoardCommandOutput extends CqrsOutput<BoardCommandOutput> {

    private UUID boardId;
    private long version;

    public UUID getBoardId() {
        return boardId;
    }

    public BoardCommandOutput setBoardId(UUID boardId) {
        this.boardId = boardId;
        return this;
    }

    public long getVersion() {
        return version;
    }

    public BoardCommandOutput setVersion(long version) {
        this.version = version;
        return this;
    }
}