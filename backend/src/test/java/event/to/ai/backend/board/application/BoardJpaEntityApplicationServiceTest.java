package event.to.ai.backend.board.application;

import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardJpaEntityApplicationServiceTest {

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
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    CreateBoardRequest request = new CreateBoardRequest("  Team Board  ", "planning");

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);

                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.of(new User()));
                    when(boardRepositoryPort.save(any(BoardJpaEntity.class))).thenAnswer(invocation -> {
                        BoardJpaEntity boardJpaEntity = invocation.getArgument(0);
                        boardJpaEntity.setId(UUID.randomUUID());
                        return boardJpaEntity;
                    });
                })
                .When("creating board", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    BoardDTO result = boardApplicationService.createBoard(actorUserId, request);
                    env.put("result", result);
                })
                .Then("result should include trimmed title and owner id", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    assertEquals("Team Board", result.getTitle());
                    assertEquals("planning", result.getDescription());
                    assertEquals(actorUserId, result.getOwnerUserId());
                })
                .Execute();
    }

    @EzScenario
    public void getAllMyBoardsShouldQueryByOwnerId() {
        Feature.New("Board Application Service")
                .newScenario("Get all my boards returns all boards owned by the authenticated user")
                .Given("a user with two owned boards", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    BoardJpaEntity firstBoardJpaEntity = new BoardJpaEntity("Roadmap", "Q1");
                    firstBoardJpaEntity.setId(UUID.fromString("00000000-0000-0000-0000-000000000101"));
                    firstBoardJpaEntity.setOwnerId(actorUserId);

                    BoardJpaEntity secondBoardJpaEntity = new BoardJpaEntity("Retrospective", "Sprint 5");
                    secondBoardJpaEntity.setId(UUID.fromString("00000000-0000-0000-0000-000000000102"));
                    secondBoardJpaEntity.setOwnerId(actorUserId);

                    env.put("actorUserId", actorUserId);
                    when(boardRepositoryPort.findAllByOwnerId(actorUserId)).thenReturn(List.of(firstBoardJpaEntity, secondBoardJpaEntity));
                })
                .When("requesting all boards for that user", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    List<BoardDTO> result = boardApplicationService.getAllMyBoards(actorUserId);
                    env.put("result", result);
                })
                .Then("service should return both owned boards", env -> {
                    List<BoardDTO> result = env.get("result", List.class);
                    assertEquals(2, result.size());
                    assertEquals("Roadmap", result.get(0).getTitle());
                    assertEquals("Retrospective", result.get(1).getTitle());
                })
                .Execute();
    }

    @EzScenario
    public void createBoardShouldThrowWhenActorUserMissing() {
        Feature.New("Board Application Service")
                .newScenario("Create board fails when actor user does not exist")
                .Given("a non-existing actor user id", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000999");
                    CreateBoardRequest request = new CreateBoardRequest("Board", "Desc");

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.empty());
                })
                .When("creating board", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    RuntimeException ex = assertThrows(
                            RuntimeException.class,
                            () -> boardApplicationService.createBoard(actorUserId, request)
                    );
                    env.put("error", ex);
                })
                .Then("error should mention missing user", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("User not found with id: 00000000-0000-0000-0000-000000000999", ex.getMessage());
                })
                .And("board should not be saved", env ->
                        verify(boardRepositoryPort, never()).save(any(BoardJpaEntity.class))
                )
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldRejectBlankTitleAfterTrim() {
        Feature.New("Board Application Service")
                .newScenario("Update board fails when title is blank after trim")
                .Given("an existing board and blank title update request", env -> {
                    UUID boardId = UUID.randomUUID();
                    BoardJpaEntity boardJpaEntity = new BoardJpaEntity("Old", "Desc");
                    boardJpaEntity.setId(boardId);
                    UpdateBoardRequest request = new UpdateBoardRequest("   ", null);

                    env.put("boardId", boardId);
                    env.put("request", request);
                    when(boardRepositoryPort.findById(boardId)).thenReturn(Optional.of(boardJpaEntity));
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
                        verify(boardRepositoryPort, never()).save(any(BoardJpaEntity.class))
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

