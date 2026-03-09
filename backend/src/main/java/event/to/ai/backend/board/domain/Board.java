package event.to.ai.backend.board.domain;

import tw.teddysoft.ezddd.entity.EsAggregateRoot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static tw.teddysoft.ucontract.Contract.ensureNotNull;

public class Board extends EsAggregateRoot<BoardId, BoardEvent> {
    private BoardId boardId;
    private BoardTitle boardTitle;
    private String description;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Board() {
    }

    public Board(List<BoardEvent> events) {
        super(events);
    }

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
            case BoardEvent.BoardRenamed e -> {
                this.boardTitle = e.title();
                this.updatedAt = LocalDateTime.ofInstant(e.occurredOn(), ZoneOffset.UTC);
            }
            case BoardEvent.BoardDescriptionChanged e -> {
                this.description = e.description();
                this.updatedAt = LocalDateTime.ofInstant(e.occurredOn(), ZoneOffset.UTC);
            }
            case BoardEvent.BoardDeleted e -> {
                this.isDeleted = true;
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

    public BoardTitle getTitle() {
        return boardTitle;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void rename(BoardTitle title) {
        if (!this.isDeleted && !this.boardTitle.equals(title)) {
            apply(new BoardEvent.BoardRenamed(boardId, title));
        }
    }

    public void changeDescription(String description) {
        if (!this.isDeleted && !this.description.equals(description)) {
            apply(new BoardEvent.BoardDescriptionChanged(boardId, description));
        }
    }

    public void delete() {
        if (!this.isDeleted) {
            apply(new BoardEvent.BoardDeleted(boardId));
        } else {
            throw new RuntimeException("board" + this.boardId.toString() + "is already deleted");
        }
    }
}