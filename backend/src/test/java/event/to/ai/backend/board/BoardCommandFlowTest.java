package event.to.ai.backend.board;

import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.application.BoardApplicationService;
import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.usecase.command.ChangeBoardDescriptionUseCase;
import event.to.ai.backend.board.application.usecase.command.CreateBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.DeleteBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.RenameBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.input.ChangeBoardDescriptionInput;
import event.to.ai.backend.board.application.usecase.command.input.CreateBoardInput;
import event.to.ai.backend.board.application.usecase.command.input.RenameBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import event.to.ai.backend.board.application.usecase.query.GetBoardByIdQuery;
import event.to.ai.backend.board.application.usecase.query.GetMyBoardsQuery;
import event.to.ai.backend.board.application.usecase.query.input.GetBoardByIdInput;
import event.to.ai.backend.board.application.usecase.query.output.GetBoardByIdOutput;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardCommandFlowTest {

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
    public void createBoardShouldBuildAggregateAndSaveIt() {
        Feature.New("Board")
                .newScenario("Create board delegates to use case and returns queried board")
                .Given("create command and follow-up query result", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000042");
                    UUID boardId = UUID.fromString("00000000-0000-0000-0000-000000000052");
                    CreateBoardRequest request = new CreateBoardRequest("  Sprint Board  ", "planning");
                    BoardCommandOutput commandOutput = new BoardCommandOutput().setBoardId(boardId).setVersion(0).succeed();
                    BoardReadModel board = new BoardReadModel(boardId, "Sprint Board", "planning", actorUserId, null, null);
                    GetBoardByIdOutput queryOutput = new GetBoardByIdOutput().setBoard(board).succeed();

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(createBoardUseCase.execute(any(CreateBoardInput.class))).thenReturn(commandOutput);
                    when(getBoardByIdQuery.execute(any(GetBoardByIdInput.class))).thenReturn(queryOutput);
                })
                .When("creating board", env -> {
                    BoardDTO result = boardApplicationService.createBoard(
                            env.get("actorUserId", UUID.class),
                            env.get("request", CreateBoardRequest.class)
                    );
                    env.put("result", result);
                })
                .Then("result should keep trimmed title and actor owner id", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("Sprint Board", result.getTitle());
                    assertEquals("planning", result.getDescription());
                    assertEquals(env.get("actorUserId", UUID.class), result.getOwnerUserId());
                })
                .And("create use case should receive request values", env -> {
                    ArgumentCaptor<CreateBoardInput> captor = ArgumentCaptor.forClass(CreateBoardInput.class);
                    verify(createBoardUseCase).execute(captor.capture());
                    CreateBoardInput input = captor.getValue();
                    assertEquals(env.get("actorUserId", UUID.class), input.getActorUserId());
                    assertEquals("  Sprint Board  ", input.getTitle());
                    assertEquals("planning", input.getDescription());
                })
                .Execute();
    }

    @EzScenario
    public void createBoardShouldFailBeforeSaveWhenTitleBlankAfterTrim() {
        Feature.New("Board")
                .newScenario("Create board surfaces command failure")
                .Given("a command that rejects blank title", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000042");
                    CreateBoardRequest request = new CreateBoardRequest("   ", "planning");
                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(createBoardUseCase.execute(any(CreateBoardInput.class)))
                            .thenThrow(new RuntimeException("Board title cannot be blank"));
                })
                .When("creating board", env -> {
                    RuntimeException ex = assertThrows(RuntimeException.class,
                            () -> boardApplicationService.createBoard(
                                    env.get("actorUserId", UUID.class),
                                    env.get("request", CreateBoardRequest.class)));
                    env.put("error", ex);
                })
                .Then("error should indicate title cannot be blank", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board title cannot be blank", ex.getMessage());
                })
                .And("query should not run", env -> verify(getBoardByIdQuery, never()).execute(any(GetBoardByIdInput.class)))
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldRenameAggregate() {
        Feature.New("Board")
                .newScenario("Update board delegates rename command and reads updated board")
                .Given("rename command succeeds and query returns updated board", env -> {
                    UUID boardId = UUID.randomUUID();
                    RenameBoardInput anyInput = new RenameBoardInput();
                    BoardCommandOutput commandOutput = new BoardCommandOutput().setBoardId(boardId).setVersion(1).succeed();
                    BoardReadModel board = new BoardReadModel(boardId, "New Team Board", "Old Desc", UUID.fromString("00000000-0000-0000-0000-000000000007"), null, null);
                    GetBoardByIdOutput queryOutput = new GetBoardByIdOutput().setBoard(board).succeed();
                    env.put("boardId", boardId);
                    env.put("request", new UpdateBoardRequest("  New Team Board  ", null));
                    when(renameBoardUseCase.execute(any(RenameBoardInput.class))).thenReturn(commandOutput);
                    when(getBoardByIdQuery.execute(any(GetBoardByIdInput.class))).thenReturn(queryOutput);
                })
                .When("updating board", env -> {
                    BoardDTO result = boardApplicationService.updateBoard(
                            env.get("boardId", UUID.class),
                            env.get("request", UpdateBoardRequest.class));
                    env.put("result", result);
                })
                .Then("result should contain updated title and preserved description", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("New Team Board", result.getTitle());
                    assertEquals("Old Desc", result.getDescription());
                })
                .And("rename use case should receive title", env -> {
                    ArgumentCaptor<RenameBoardInput> captor = ArgumentCaptor.forClass(RenameBoardInput.class);
                    verify(renameBoardUseCase).execute(captor.capture());
                    assertEquals("  New Team Board  ", captor.getValue().getTitle());
                })
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldChangeDescriptionThroughAggregate() {
        Feature.New("Board")
                .newScenario("Update board delegates description command and reads updated board")
                .Given("description command succeeds and query returns updated board", env -> {
                    UUID boardId = UUID.randomUUID();
                    BoardCommandOutput commandOutput = new BoardCommandOutput().setBoardId(boardId).setVersion(1).succeed();
                    BoardReadModel board = new BoardReadModel(boardId, "Stable Title", "Only Desc Changed", UUID.fromString("00000000-0000-0000-0000-000000000009"), null, null);
                    GetBoardByIdOutput queryOutput = new GetBoardByIdOutput().setBoard(board).succeed();
                    env.put("boardId", boardId);
                    env.put("request", new UpdateBoardRequest(null, "Only Desc Changed"));
                    when(changeBoardDescriptionUseCase.execute(any(ChangeBoardDescriptionInput.class))).thenReturn(commandOutput);
                    when(getBoardByIdQuery.execute(any(GetBoardByIdInput.class))).thenReturn(queryOutput);
                })
                .When("updating board", env -> {
                    BoardDTO result = boardApplicationService.updateBoard(
                            env.get("boardId", UUID.class),
                            env.get("request", UpdateBoardRequest.class));
                    env.put("result", result);
                })
                .Then("result should contain updated description", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("Stable Title", result.getTitle());
                    assertEquals("Only Desc Changed", result.getDescription());
                })
                .And("description use case should receive description", env -> {
                    ArgumentCaptor<ChangeBoardDescriptionInput> captor = ArgumentCaptor.forClass(ChangeBoardDescriptionInput.class);
                    verify(changeBoardDescriptionUseCase).execute(captor.capture());
                    assertEquals("Only Desc Changed", captor.getValue().getDescription());
                })
                .Execute();
    }
}