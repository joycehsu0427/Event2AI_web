package event.to.ai.backend.board.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardAggregateTest {

    @Test
    void createShouldApplyBoardCreatedEventAndSetState() {
        BoardId boardId = BoardId.valueOf(UUID.fromString("00000000-0000-0000-0000-000000000101"));
        BoardTitle title = BoardTitle.valueOf("Planning Board");
        UUID ownerId = UUID.fromString("00000000-0000-0000-0000-000000000201");

        Board board = Board.create(boardId, title, "first draft", ownerId);

        assertEquals(boardId, board.getId());
        assertEquals(title, board.getTitle());
        assertEquals("first draft", board.getDescription());
        assertEquals(ownerId, board.getOwnerId());
        assertEquals(1, board.getDomainEvents().size());
        BoardEvent event = board.getDomainEvents().getFirst();
        BoardEvent.BoardCreated created = assertInstanceOf(BoardEvent.BoardCreated.class, event);
        assertEquals(boardId, created.boardId());
        assertEquals(title, created.title());
        assertEquals("first draft", created.description());
        assertEquals(ownerId, created.ownerId());
        assertFalse(board.isDeleted());
    }

    @Test
    void renameShouldAppendBoardRenamedEventAndUpdateTitle() {
        Board board = Board.create(BoardId.create(), BoardTitle.valueOf("Old"), "desc", UUID.randomUUID());

        board.rename(BoardTitle.valueOf("New"));

        assertEquals(BoardTitle.valueOf("New"), board.getTitle());
        assertEquals(2, board.getDomainEvents().size());
        assertInstanceOf(BoardEvent.BoardRenamed.class, board.getDomainEvents().get(1));
    }

    @Test
    void changeDescriptionShouldAppendBoardDescriptionChangedEventAndUpdateDescription() {
        Board board = Board.create(BoardId.create(), BoardTitle.valueOf("Board"), "old desc", UUID.randomUUID());

        board.changeDescription("new desc");

        assertEquals("new desc", board.getDescription());
        assertEquals(2, board.getDomainEvents().size());
        BoardEvent.BoardDescriptionChanged changed = assertInstanceOf(
                BoardEvent.BoardDescriptionChanged.class,
                board.getDomainEvents().get(1)
        );
        assertEquals("new desc", changed.description());
    }

    @Test
    void rehydrateShouldRestoreStateAndNotKeepUncommittedEvents() {
        BoardId boardId = BoardId.valueOf(UUID.fromString("00000000-0000-0000-0000-000000000102"));
        UUID ownerId = UUID.fromString("00000000-0000-0000-0000-000000000202");
        Instant createdAt = Instant.parse("2026-03-09T10:15:30Z");
        Instant descriptionChangedAt = Instant.parse("2026-03-09T10:18:30Z");
        Instant renamedAt = Instant.parse("2026-03-09T10:20:30Z");

        Board board = new Board(List.of(
                new BoardEvent.BoardCreated(
                        UUID.fromString("10000000-0000-0000-0000-000000000001"),
                        createdAt,
                        boardId.toString(),
                        Map.of(),
                        boardId,
                        ownerId,
                        BoardTitle.valueOf("Original"),
                        "draft"
                ),
                new BoardEvent.BoardDescriptionChanged(
                        UUID.fromString("10000000-0000-0000-0000-000000000002"),
                        descriptionChangedAt,
                        boardId.toString(),
                        Map.of(),
                        boardId,
                        "updated draft"
                ),
                new BoardEvent.BoardRenamed(
                        UUID.fromString("10000000-0000-0000-0000-000000000003"),
                        renamedAt,
                        boardId.toString(),
                        Map.of(),
                        boardId,
                        BoardTitle.valueOf("Renamed")
                )
        ));

        assertEquals(boardId, board.getId());
        assertEquals(BoardTitle.valueOf("Renamed"), board.getTitle());
        assertEquals("updated draft", board.getDescription());
        assertEquals(ownerId, board.getOwnerId());
        assertEquals(LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC), board.getCreatedAt());
        assertEquals(LocalDateTime.ofInstant(renamedAt, ZoneOffset.UTC), board.getUpdatedAt());
        assertTrue(board.getDomainEvents().isEmpty());
        assertFalse(board.isDeleted());
    }

    @Test
    void deleteShouldAppendBoardDeletedEventAndMarkAggregateDeleted() {
        Board board = Board.create(BoardId.create(), BoardTitle.valueOf("Board"), "desc", UUID.randomUUID());

        board.delete();

        assertTrue(board.isDeleted());
        assertEquals(2, board.getDomainEvents().size());
        assertInstanceOf(BoardEvent.BoardDeleted.class, board.getDomainEvents().get(1));
    }

    @Test
    void renameAfterDeleteShouldNotCreateAnotherEvent() {
        Board board = Board.create(BoardId.create(), BoardTitle.valueOf("Board"), "desc", UUID.randomUUID());
        board.delete();

        board.rename(BoardTitle.valueOf("Should Not Apply"));

        assertTrue(board.isDeleted());
        assertEquals(2, board.getDomainEvents().size());
        assertEquals(BoardTitle.valueOf("Board"), board.getTitle());
        assertInstanceOf(BoardEvent.BoardDeleted.class, board.getDomainEvents().get(1));
    }
}