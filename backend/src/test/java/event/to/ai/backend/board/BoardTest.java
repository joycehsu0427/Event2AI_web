package event.to.ai.backend.board;

import event.to.ai.backend.board.application.BoardApplicationService;
import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardTest {

    @Mock
    private BoardRepositoryPort boardRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private BoardApplicationService boardApplicationService;

    @EzScenario
    public void createBoardShouldTrimTitleAndBindOwnerIdFromActor() {
        Feature.New("Board")
                .newScenario("Create board trims title and uses actor user id as owner id")
                .Given("an authenticated actor and create request", env -> {
                    Long actorUserId = 42L;
                    CreateBoardRequest request = new CreateBoardRequest("  Sprint Board  ", "planning");
                    User actor = new User("actor", "actor@example.com", "hash");
                    actor.setId(actorUserId);

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.of(actor));
                    when(boardRepositoryPort.save(any(Board.class))).thenAnswer(invocation -> {
                        Board board = invocation.getArgument(0);
                        board.setId(UUID.randomUUID());
                        return board;
                    });
                })
                .When("creating board", env -> {
                    Long actorUserId = env.get("actorUserId", Long.class);
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    BoardDTO result = boardApplicationService.createBoard(actorUserId, request);
                    env.put("result", result);
                })
                .Then("result should keep trimmed title and actor owner id", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("Sprint Board", result.getTitle());
                    assertEquals("planning", result.getDescription());
                    assertEquals(42L, result.getOwnerUserId());
                })
                .And("saved board should be persisted with actor owner id", env ->
                        verify(boardRepositoryPort).save(argThat(board ->
                                "Sprint Board".equals(board.getTitle()) &&
                                        "planning".equals(board.getDescription()) &&
                                        42L == board.getOwnerId()
                        ))
                )
                .Execute();
    }

    @EzScenario
    public void createBoardShouldFailBeforeSaveWhenTitleBlankAfterTrim() {
        Feature.New("Board")
                .newScenario("Create board rejects blank title after trim")
                .Given("an authenticated actor and blank title request", env -> {
                    Long actorUserId = 42L;
                    CreateBoardRequest request = new CreateBoardRequest("   ", "planning");
                    User actor = new User("actor", "actor@example.com", "hash");
                    actor.setId(actorUserId);

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.of(actor));
                })
                .When("creating board", env -> {
                    Long actorUserId = env.get("actorUserId", Long.class);
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    RuntimeException ex = assertThrows(RuntimeException.class,
                            () -> boardApplicationService.createBoard(actorUserId, request));
                    env.put("error", ex);
                })
                .Then("error should indicate title cannot be blank", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board title cannot be blank", ex.getMessage());
                })
                .And("board should not be saved", env -> verify(boardRepositoryPort, never()).save(any(Board.class)))
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldTrimTitleUpdateDescriptionAndKeepOwner() {
        Feature.New("Board")
                .newScenario("Update board applies trimmed title and keeps existing owner")
                .Given("an existing board with owner and update request", env -> {
                    UUID boardId = UUID.randomUUID();
                    Board board = new Board("Old Title", "Old Desc");
                    board.setId(boardId);
                    board.setOwnerId(7L);

                    UpdateBoardRequest request = new UpdateBoardRequest("  New Team Board  ", "New Desc");

                    env.put("boardId", boardId);
                    env.put("request", request);
                    when(boardRepositoryPort.findById(boardId)).thenReturn(Optional.of(board));
                    when(boardRepositoryPort.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));
                })
                .When("updating board", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    BoardDTO result = boardApplicationService.updateBoard(boardId, request);
                    env.put("result", result);
                })
                .Then("result should contain updated title and description", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("New Team Board", result.getTitle());
                    assertEquals("New Desc", result.getDescription());
                    assertEquals(7L, result.getOwnerUserId());
                })
                .And("saved board should preserve owner while applying trimmed updates", env ->
                        verify(boardRepositoryPort).save(argThat(board ->
                                "New Team Board".equals(board.getTitle()) &&
                                        "New Desc".equals(board.getDescription()) &&
                                        7L == board.getOwnerId()
                        ))
                )
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldOnlyUpdateDescriptionWhenTitleIsNull() {
        Feature.New("Board")
                .newScenario("Update board keeps original title when title input is null")
                .Given("an existing board and description-only update", env -> {
                    UUID boardId = UUID.randomUUID();
                    Board board = new Board("Stable Title", "Old Desc");
                    board.setId(boardId);
                    board.setOwnerId(9L);

                    UpdateBoardRequest request = new UpdateBoardRequest(null, "Only Desc Changed");

                    env.put("boardId", boardId);
                    env.put("request", request);
                    when(boardRepositoryPort.findById(boardId)).thenReturn(Optional.of(board));
                    when(boardRepositoryPort.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));
                })
                .When("updating board", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    BoardDTO result = boardApplicationService.updateBoard(boardId, request);
                    env.put("result", result);
                })
                .Then("result should keep original title and update description", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("Stable Title", result.getTitle());
                    assertEquals("Only Desc Changed", result.getDescription());
                    assertEquals(9L, result.getOwnerUserId());
                })
                .And("saved board should preserve title and owner id", env ->
                        verify(boardRepositoryPort).save(argThat(board ->
                                "Stable Title".equals(board.getTitle()) &&
                                        "Only Desc Changed".equals(board.getDescription()) &&
                                        9L == board.getOwnerId()
                        ))
                )
                .Execute();
    }
}
