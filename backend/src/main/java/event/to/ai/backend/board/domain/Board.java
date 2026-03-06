package event.to.ai.backend.board.domain;

import tw.teddysoft.ezddd.entity.EsAggregateRoot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static tw.teddysoft.ucontract.Contract.ensureNotNull;

public class Board extends EsAggregateRoot<BoardId, BoardEvent> {
    private BoardId boardId;
    private BoardTitle boardTitle;
    private String description;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    protected void when(BoardEvent boardEvent) {
        switch (boardEvent) {
            case BoardEvent.BoardCreated e -> {
                this.boardId = e.boardId();
                this.boardTitle = e.title();
                this.description = e.description();
                this.ownerId = e.ownerId();
                this.isDeleted = false;
                this.createdAt = LocalDateTime.ofInstant(e.occurredOn(), ZoneOffset.UTC);
                this.updatedAt = this.createdAt;
            }
            case BoardEvent.BoardDeleted e -> {
                this.isDeleted = true;
                this.updatedAt = LocalDateTime.ofInstant(e.occurredOn(), ZoneOffset.UTC);
            }
            case BoardEvent.BoardRenamed e -> {
                this.boardTitle = e.title();
                this.updatedAt = LocalDateTime.ofInstant(e.occurredOn(), ZoneOffset.UTC);
            }
        }
    }

    @Override
    protected void ensureInvariant() {
        ensureNotNull("boardId", boardId);
        ensureNotNull("boardTitle", boardTitle);
        ensureNotNull("description", description);
        ensureNotNull("ownerId", ownerId);
    }

    @Override
    public BoardId getId() {
        return boardId;
    }

    @Override
    public String getCategory() {
        return "board";
    }

    public static Board create(BoardId id, BoardTitle title, String description, UUID ownerId) {
        Board board = new Board();
        board.apply(new BoardEvent.BoardCreated(id, title, description, ownerId));
        return board;
    }

    public void rename(BoardTitle title) {
        if (this.isDeleted == false){
            apply(new BoardEvent.BoardRenamed(boardId, title));
        }
    }

    public void delete() {
        apply(new BoardEvent.BoardDeleted(boardId));
    }
}
