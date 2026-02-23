package event.to.ai.backend.board.application;

import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.dto.BoardDTO;
import event.to.ai.backend.dto.CreateBoardRequest;
import event.to.ai.backend.dto.UpdateBoardRequest;
import event.to.ai.backend.entity.Board;
import event.to.ai.backend.entity.User;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardApplicationServiceTest {

    @Mock
    private BoardRepositoryPort boardRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private BoardApplicationService boardApplicationService;

    @EzScenario
    public void createBoardShouldUseAuthenticatedUserAsOwner() {
        Feature.New("Board Application Service")
                .newScenario("Create board uses authenticated user as owner")
                .Given("a valid actor and create-board request", env -> {
                    Long actorUserId = 1L;
                    CreateBoardRequest request = new CreateBoardRequest("  Team Board  ", "planning");

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);

                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.of(new User()));
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
                .Then("result should include trimmed title and owner id", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    Long actorUserId = env.get("actorUserId", Long.class);
                    assertEquals("Team Board", result.getTitle());
                    assertEquals("planning", result.getDescription());
                    assertEquals(actorUserId, result.getOwnerUserId());
                })
                .Execute();
    }

    @EzScenario
    public void createBoardShouldThrowWhenActorUserMissing() {
        Feature.New("Board Application Service")
                .newScenario("Create board fails when actor user does not exist")
                .Given("a non-existing actor user id", env -> {
                    Long actorUserId = 999L;
                    CreateBoardRequest request = new CreateBoardRequest("Board", "Desc");

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.empty());
                })
                .When("creating board", env -> {
                    Long actorUserId = env.get("actorUserId", Long.class);
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    RuntimeException ex = assertThrows(
                            RuntimeException.class,
                            () -> boardApplicationService.createBoard(actorUserId, request)
                    );
                    env.put("error", ex);
                })
                .Then("error should mention missing user", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("User not found with id: 999", ex.getMessage());
                })
                .And("board should not be saved", env ->
                        verify(boardRepositoryPort, never()).save(any(Board.class))
                )
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldRejectBlankTitleAfterTrim() {
        Feature.New("Board Application Service")
                .newScenario("Update board fails when title is blank after trim")
                .Given("an existing board and blank title update request", env -> {
                    UUID boardId = UUID.randomUUID();
                    Board board = new Board("Old", "Desc");
                    board.setId(boardId);
                    UpdateBoardRequest request = new UpdateBoardRequest("   ", null);

                    env.put("boardId", boardId);
                    env.put("request", request);
                    when(boardRepositoryPort.findById(boardId)).thenReturn(Optional.of(board));
                })
                .When("updating board", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    RuntimeException ex = assertThrows(
                            RuntimeException.class,
                            () -> boardApplicationService.updateBoard(boardId, request)
                    );
                    env.put("error", ex);
                })
                .Then("error should indicate blank title is invalid", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board title cannot be blank", ex.getMessage());
                })
                .And("board should not be saved", env ->
                        verify(boardRepositoryPort, never()).save(any(Board.class))
                )
                .Execute();
    }

    @EzScenario
    public void deleteBoardShouldThrowWhenNotFound() {
        Feature.New("Board Application Service")
                .newScenario("Delete board fails when board id does not exist")
                .Given("a non-existing board id", env -> {
                    UUID boardId = UUID.randomUUID();
                    env.put("boardId", boardId);
                    when(boardRepositoryPort.existsById(boardId)).thenReturn(false);
                })
                .When("deleting board", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    RuntimeException ex = assertThrows(
                            RuntimeException.class,
                            () -> boardApplicationService.deleteBoard(boardId)
                    );
                    env.put("error", ex);
                })
                .Then("error should mention board not found", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board not found with id: " + boardId, ex.getMessage());
                })
                .And("delete should not be called", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    verify(boardRepositoryPort, never()).deleteById(boardId);
                })
                .Execute();
    }
}
