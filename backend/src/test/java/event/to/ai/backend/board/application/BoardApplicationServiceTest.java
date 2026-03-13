package event.to.ai.backend.board.application;

import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.usecase.command.ChangeBoardDescriptionUseCase;
import event.to.ai.backend.board.application.usecase.command.CreateBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.DeleteBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.RenameBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.input.CreateBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import event.to.ai.backend.board.application.usecase.query.GetBoardByIdQuery;
import event.to.ai.backend.board.application.usecase.query.GetMyBoardsQuery;
import event.to.ai.backend.board.application.usecase.query.input.GetBoardByIdInput;
import event.to.ai.backend.board.application.usecase.query.output.GetBoardByIdOutput;
import event.to.ai.backend.board.application.usecase.query.output.GetMyBoardsOutput;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.List;
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
    private CreateBoardUseCase createBoardUseCase;

    @Mock
    private RenameBoardUseCase renameBoardUseCase;

    @Mock
    private ChangeBoardDescriptionUseCase changeBoardDescriptionUseCase;

    @Mock
    private DeleteBoardUseCase deleteBoardUseCase;

    @Mock
    private GetBoardByIdQuery getBoardByIdQuery;

    @Mock
    private GetMyBoardsQuery getMyBoardsQuery;

    @InjectMocks
    private BoardApplicationService boardApplicationService;

    @EzScenario
    public void createBoardShouldUseAuthenticatedUserAsOwner() {
        Feature.New("Board Application Service")
                .newScenario("Create board delegates command and query")
                .Given("a valid actor and create-board request", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    UUID boardId = UUID.fromString("00000000-0000-0000-0000-000000000011");
                    CreateBoardRequest request = new CreateBoardRequest("  Team Board  ", "planning");
                    BoardCommandOutput commandOutput = new BoardCommandOutput().setBoardId(boardId).setVersion(0).succeed();
                    BoardReadModel board = new BoardReadModel(boardId, "Team Board", "planning", actorUserId, null, null);
                    GetBoardByIdOutput queryOutput = new GetBoardByIdOutput().setBoard(board).succeed();

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(createBoardUseCase.execute(any(CreateBoardInput.class))).thenReturn(commandOutput);
                    when(getBoardByIdQuery.execute(any(GetBoardByIdInput.class))).thenReturn(queryOutput);
                })
                .When("creating board", env -> {
                    BoardDTO result = boardApplicationService.createBoard(
                            env.get("actorUserId", UUID.class),
                            env.get("request", CreateBoardRequest.class));
                    env.put("result", result);
                })
                .Then("result should include trimmed title and owner id", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("Team Board", result.getTitle());
                    assertEquals("planning", result.getDescription());
                    assertEquals(env.get("actorUserId", UUID.class), result.getOwnerUserId());
                })
                .And("command should receive request", env -> {
                    ArgumentCaptor<CreateBoardInput> captor = ArgumentCaptor.forClass(CreateBoardInput.class);
                    verify(createBoardUseCase).execute(captor.capture());
                    assertEquals("  Team Board  ", captor.getValue().getTitle());
                })
                .Execute();
    }

    @EzScenario
    public void getAllMyBoardsShouldQueryByOwnerId() {
        Feature.New("Board Application Service")
                .newScenario("Get all my boards returns all boards owned by the authenticated user")
                .Given("a user with two owned boards", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    BoardReadModel firstBoard = new BoardReadModel(UUID.fromString("00000000-0000-0000-0000-000000000101"), "Roadmap", "Q1", actorUserId, null, null);
                    BoardReadModel secondBoard = new BoardReadModel(UUID.fromString("00000000-0000-0000-0000-000000000102"), "Retrospective", "Sprint 5", actorUserId, null, null);
                    GetMyBoardsOutput output = new GetMyBoardsOutput().setBoards(List.of(firstBoard, secondBoard)).succeed();
                    env.put("actorUserId", actorUserId);
                    when(getMyBoardsQuery.execute(any())).thenReturn(output);
                })
                .When("requesting all boards for that user", env -> {
                    List<BoardDTO> result = boardApplicationService.getAllMyBoards(env.get("actorUserId", UUID.class));
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
    public void createBoardShouldThrowWhenCommandFails() {
        Feature.New("Board Application Service")
                .newScenario("Create board surfaces command failure")
                .Given("a create command failure", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000999");
                    CreateBoardRequest request = new CreateBoardRequest("Board", "Desc");
                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(createBoardUseCase.execute(any(CreateBoardInput.class)))
                            .thenThrow(new RuntimeException("User not found with id: 00000000-0000-0000-0000-000000000999"));
                })
                .When("creating board", env -> {
                    RuntimeException ex = assertThrows(RuntimeException.class,
                            () -> boardApplicationService.createBoard(env.get("actorUserId", UUID.class), env.get("request", CreateBoardRequest.class)));
                    env.put("error", ex);
                })
                .Then("error should mention missing user", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("User not found with id: 00000000-0000-0000-0000-000000000999", ex.getMessage());
                })
                .And("query should not be called", env -> verify(getBoardByIdQuery, never()).execute(any(GetBoardByIdInput.class)))
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldRejectBlankTitleAfterTrim() {
        Feature.New("Board Application Service")
                .newScenario("Update board fails when title is blank after trim")
                .Given("a rename command failure", env -> {
                    UUID boardId = UUID.randomUUID();
                    env.put("boardId", boardId);
                    env.put("request", new UpdateBoardRequest("   ", null));
                    when(renameBoardUseCase.execute(any())).thenThrow(new RuntimeException("Board title cannot be blank"));
                })
                .When("updating board", env -> {
                    RuntimeException ex = assertThrows(RuntimeException.class,
                            () -> boardApplicationService.updateBoard(env.get("boardId", UUID.class), env.get("request", UpdateBoardRequest.class)));
                    env.put("error", ex);
                })
                .Then("error should indicate blank title is invalid", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board title cannot be blank", ex.getMessage());
                })
                .And("query should not run", env -> verify(getBoardByIdQuery, never()).execute(any(GetBoardByIdInput.class)))
                .Execute();
    }

    @EzScenario
    public void deleteBoardShouldThrowWhenNotFound() {
        Feature.New("Board Application Service")
                .newScenario("Delete board fails when board id does not exist")
                .Given("a delete command failure", env -> {
                    UUID boardId = UUID.randomUUID();
                    env.put("boardId", boardId);
                    when(deleteBoardUseCase.execute(any())).thenThrow(new RuntimeException("Board not found with id: " + boardId));
                })
                .When("deleting board", env -> {
                    RuntimeException ex = assertThrows(RuntimeException.class,
                            () -> boardApplicationService.deleteBoard(env.get("boardId", UUID.class)));
                    env.put("error", ex);
                })
                .Then("error should mention board not found", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board not found with id: " + env.get("boardId", UUID.class), ex.getMessage());
                })
                .Execute();
    }
}