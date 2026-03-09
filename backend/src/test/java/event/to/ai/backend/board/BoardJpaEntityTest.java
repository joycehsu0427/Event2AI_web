package event.to.ai.backend.board;

import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.application.BoardApplicationService;
import event.to.ai.backend.board.application.port.out.BoardEventRepositoryPort;
import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardId;
import event.to.ai.backend.board.domain.BoardTitle;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class BoardJpaEntityTest {

    @Mock
    private BoardRepositoryPort boardRepositoryPort;

    @Mock
    private BoardEventRepositoryPort boardEventRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private BoardApplicationService boardApplicationService;

    @EzScenario
    public void createBoardShouldBuildAggregateAndSaveIt() {
        Feature.New("Board")
                .newScenario("Create board trims title and saves aggregate through event repository")
                .Given("an authenticated actor and create request", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000042");
                    CreateBoardRequest request = new CreateBoardRequest("  Sprint Board  ", "planning");
                    User actor = new User("actor", "actor@example.com", "hash");
                    actor.setId(actorUserId);

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.of(actor));
                })
                .When("creating board", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    BoardDTO result = boardApplicationService.createBoard(actorUserId, request);
                    env.put("result", result);
                })
                .Then("result should keep trimmed title and actor owner id", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("Sprint Board", result.getTitle());
                    assertEquals("planning", result.getDescription());
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000042"), result.getOwnerUserId());
                })
                .And("saved aggregate should carry the same values", env -> {
                    ArgumentCaptor<Board> captor = ArgumentCaptor.forClass(Board.class);
                    verify(boardEventRepositoryPort).save(captor.capture());
                    Board savedBoard = captor.getValue();
                    assertEquals(BoardTitle.valueOf("Sprint Board"), savedBoard.getTitle());
                    assertEquals("planning", savedBoard.getDescription());
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000042"), savedBoard.getOwnerId());
                })
                .Execute();
    }

    @EzScenario
    public void createBoardShouldFailBeforeSaveWhenTitleBlankAfterTrim() {
        Feature.New("Board")
                .newScenario("Create board rejects blank title after trim")
                .Given("an authenticated actor and blank title request", env -> {
                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000042");
                    CreateBoardRequest request = new CreateBoardRequest("   ", "planning");
                    User actor = new User("actor", "actor@example.com", "hash");
                    actor.setId(actorUserId);

                    env.put("actorUserId", actorUserId);
                    env.put("request", request);
                    when(userRepositoryPort.findById(actorUserId)).thenReturn(Optional.of(actor));
                })
                .When("creating board", env -> {
                    UUID actorUserId = env.get("actorUserId", UUID.class);
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    RuntimeException ex = assertThrows(RuntimeException.class,
                            () -> boardApplicationService.createBoard(actorUserId, request));
                    env.put("error", ex);
                })
                .Then("error should indicate title cannot be blank", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board title cannot be blank", ex.getMessage());
                })
                .And("aggregate should not be saved", env -> verify(boardEventRepositoryPort, never()).save(any(Board.class)))
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldRenameAggregate() {
        Feature.New("Board")
                .newScenario("Update board renames aggregate when title is provided")
                .Given("an existing aggregate and title update request", env -> {
                    UUID boardId = UUID.randomUUID();
                    Board board = Board.create(BoardId.valueOf(boardId), BoardTitle.valueOf("Old Title"), "Old Desc", UUID.fromString("00000000-0000-0000-0000-000000000007"));
                    board.clearDomainEvents();

                    UpdateBoardRequest request = new UpdateBoardRequest("  New Team Board  ", null);

                    env.put("boardId", boardId);
                    env.put("request", request);
                    when(boardEventRepositoryPort.findById(BoardId.valueOf(boardId))).thenReturn(Optional.of(board));
                })
                .When("updating board", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    BoardDTO result = boardApplicationService.updateBoard(boardId, request);
                    env.put("result", result);
                })
                .Then("result should contain updated title and preserved description", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("New Team Board", result.getTitle());
                    assertEquals("Old Desc", result.getDescription());
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000007"), result.getOwnerUserId());
                })
                .And("aggregate should be saved after rename", env -> verify(boardEventRepositoryPort).save(any(Board.class)))
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldChangeDescriptionThroughAggregate() {
        Feature.New("Board")
                .newScenario("Update board changes description through aggregate behavior")
                .Given("an existing aggregate and description update", env -> {
                    UUID boardId = UUID.randomUUID();
                    Board board = Board.create(BoardId.valueOf(boardId), BoardTitle.valueOf("Stable Title"), "Old Desc", UUID.fromString("00000000-0000-0000-0000-000000000009"));
                    board.clearDomainEvents();

                    UpdateBoardRequest request = new UpdateBoardRequest(null, "Only Desc Changed");

                    env.put("boardId", boardId);
                    env.put("request", request);
                    when(boardEventRepositoryPort.findById(BoardId.valueOf(boardId))).thenReturn(Optional.of(board));
                })
                .When("updating board", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    BoardDTO result = boardApplicationService.updateBoard(boardId, request);
                    env.put("result", result);
                })
                .Then("result should contain updated description", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("Stable Title", result.getTitle());
                    assertEquals("Only Desc Changed", result.getDescription());
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000009"), result.getOwnerUserId());
                })
                .And("aggregate should be saved after description change", env -> verify(boardEventRepositoryPort).save(any(Board.class)))
                .Execute();
    }
}