package event.to.ai.backend.stickynote.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.stickynote.adapter.in.web.dto.UpdateStickyNoteRequest;
import event.to.ai.backend.stickynote.adapter.in.web.dto.StickyNoteDTO;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;
import event.to.ai.backend.stickynote.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.stickynote.application.port.out.StickyNoteRepositoryPort;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StickyNoteApplicationServiceTest {

    @Mock
    private StickyNoteRepositoryPort stickyNoteRepositoryPort;

    @Mock
    private BoardRepositoryPort boardRepositoryPort;

    @InjectMocks
    private StickyNoteApplicationService stickyNoteApplicationService;

    @EzScenario
    public void getAllStickyNotesShouldOnlyReturnOwnedNotes() {
        Feature.New("StickyNote Application Service")
                .newScenario("Get all sticky notes filters out notes not owned by current user")
                .Given("sticky notes across two different board owners", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");

                    StickyNote ownedNote = createStickyNote(
                            UUID.fromString("00000000-0000-0000-0000-000000000011"),
                            UUID.fromString("00000000-0000-0000-0000-000000000101"),
                            actorUserId,
                            "yellow"
                    );
                    StickyNote foreignNote = createStickyNote(
                            UUID.fromString("00000000-0000-0000-0000-000000000012"),
                            UUID.fromString("00000000-0000-0000-0000-000000000102"),
                            otherUserId,
                            "blue"
                    );

                    env.put("actorUserId", actorUserId);
                    when(stickyNoteRepositoryPort.findAll()).thenReturn(List.of(ownedNote, foreignNote));
                })
                .When("reading all sticky notes", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    List<StickyNoteDTO> result = stickyNoteApplicationService.getAllStickyNotes(actorUserId);
                    env.put("result", result);
                })
                .Then("only owned sticky notes should be returned", env -> {
                    List<StickyNoteDTO> result = env.get("result", List.class);
                    assertEquals(1, result.size());
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000011"), result.get(0).getId());
                    assertEquals("yellow", result.get(0).getColor());
                })
                .Execute();
    }

    @EzScenario
    public void getStickyNotesByIdShouldHideForeignNote() {
        Feature.New("StickyNote Application Service")
                .newScenario("Get sticky note by id returns empty when note belongs to another user")
                .Given("a sticky note owned by a different user", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
                    UUID stickyNoteId = UUID.fromString("00000000-0000-0000-0000-000000000022");

                    StickyNote foreignNote = createStickyNote(
                            stickyNoteId,
                            UUID.fromString("00000000-0000-0000-0000-000000000202"),
                            otherUserId,
                            "green"
                    );

                    env.put("actorUserId", actorUserId);
                    env.put("stickyNoteId", stickyNoteId);
                    when(stickyNoteRepositoryPort.findById(stickyNoteId)).thenReturn(Optional.of(foreignNote));
                })
                .When("reading sticky note by id", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    UUID stickyNoteId = env.get("stickyNoteId", UUID.class);
                    List<StickyNoteDTO> result = stickyNoteApplicationService.getStickyNotesById(actorUserId, stickyNoteId);
                    env.put("result", result);
                })
                .Then("service should not expose the foreign note", env -> {
                    List<StickyNoteDTO> result = env.get("result", List.class);
                    assertTrue(result.isEmpty());
                })
                .Execute();
    }

    @EzScenario
    public void updateStickyNoteShouldRejectMovingToForeignBoard() {
        Feature.New("StickyNote Application Service")
                .newScenario("Update sticky note rejects changing board to another user's board")
                .Given("an owned sticky note and a target board owned by someone else", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
                    UUID stickyNoteId = UUID.fromString("00000000-0000-0000-0000-000000000033");
                    UUID foreignBoardId = UUID.fromString("00000000-0000-0000-0000-000000000303");

                    StickyNote ownedNote = createStickyNote(
                            stickyNoteId,
                            UUID.fromString("00000000-0000-0000-0000-000000000301"),
                            actorUserId,
                            "pink"
                    );
                    Board foreignBoard = new Board("Foreign Board", "Description");
                    foreignBoard.setId(foreignBoardId);
                    foreignBoard.setOwnerId(otherUserId);

                    UpdateStickyNoteRequest request = new UpdateStickyNoteRequest(
                            foreignBoardId,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );

                    env.put("actorUserId", actorUserId);
                    env.put("stickyNoteId", stickyNoteId);
                    env.put("request", request);
                    when(stickyNoteRepositoryPort.findById(stickyNoteId)).thenReturn(Optional.of(ownedNote));
                    when(boardRepositoryPort.findById(foreignBoardId)).thenReturn(Optional.of(foreignBoard));
                })
                .When("updating sticky note board", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    UUID stickyNoteId = env.get("stickyNoteId", UUID.class);
                    UpdateStickyNoteRequest request = env.get("request", UpdateStickyNoteRequest.class);

                    RuntimeException ex = assertThrows(
                            RuntimeException.class,
                            () -> stickyNoteApplicationService.updateStickyNote(actorUserId, stickyNoteId, request)
                    );
                    env.put("error", ex);
                })
                .Then("service should reject the move as forbidden", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Forbidden", ex.getMessage());
                })
                .Execute();
    }

    private StickyNote createStickyNote(UUID stickyNoteId, UUID boardId, UUID ownerId, String color) {
        Board board = new Board("Board", "Description");
        board.setId(boardId);
        board.setOwnerId(ownerId);

        StickyNote stickyNote = new StickyNote();
        stickyNote.setId(stickyNoteId);
        stickyNote.setBoard(board);
        stickyNote.setPos(new Point2D(10.0, 20.0));
        stickyNote.setGeo(new Point2D(100.0, 50.0));
        stickyNote.setDescription("Demo note");
        stickyNote.setColor(color);
        stickyNote.setTag("tag");
        return stickyNote;
    }
}

