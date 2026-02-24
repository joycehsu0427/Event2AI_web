package event.to.ai.backend.service;

import event.to.ai.backend.dto.BoardDTO;
import event.to.ai.backend.dto.CreateBoardRequest;
import event.to.ai.backend.dto.UpdateBoardRequest;
import event.to.ai.backend.entity.Board;
import event.to.ai.backend.repository.BoardRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @EzScenario
    public void createBoardShouldTrimTitleAndPersist() {
        Feature.New("Board Service")
                .newScenario("Create board trims title and persists")
                .Given("a board creation request with surrounding spaces in title", env -> {
                    CreateBoardRequest request = new CreateBoardRequest("  Team Board  ", "planning board");
                    UUID boardId = UUID.randomUUID();

                    env.put("request", request);
                    env.put("boardId", boardId);

                    when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
                        Board board = invocation.getArgument(0);
                        board.setId(boardId);
                        return board;
                    });
                })
                .When("creating the board", env -> {
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    BoardDTO result = boardService.createBoard(request);
                    env.put("result", result);
                })
                .Then("board DTO should contain trimmed title and persisted values", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    BoardDTO result = env.get("result", BoardDTO.class);

                    assertEquals(boardId, result.getId());
                    assertEquals("Team Board", result.getTitle());
                    assertEquals("planning board", result.getDescription());
                })
                .And("repository should be called with trimmed title", env -> {
                    ArgumentMatcher<Board> matcher = board ->
                            "Team Board".equals(board.getTitle()) &&
                                    "planning board".equals(board.getDescription());
                    verify(boardRepository).save(argThat(matcher));
                })
                .Execute();
    }

    @EzScenario
    public void getAllBoardsShouldReturnMappedDtos() {
        Feature.New("Board Service")
                .newScenario("Get all boards returns mapped DTO list")
                .Given("two boards in repository", env -> {
                    UUID id1 = UUID.randomUUID();
                    UUID id2 = UUID.randomUUID();

                    Board board1 = new Board("Board 1", "Desc 1");
                    board1.setId(id1);
                    Board board2 = new Board("Board 2", "Desc 2");
                    board2.setId(id2);

                    when(boardRepository.findAll()).thenReturn(List.of(board1, board2));
                })
                .When("getting all boards", env -> {
                    List<BoardDTO> result = boardService.getAllBoards();
                    env.put("result", result);
                })
                .Then("DTO list should contain both boards", env -> {
                    List<BoardDTO> result = env.get("result", List.class);
                    assertEquals(2, result.size());
                    assertEquals("Board 1", result.get(0).getTitle());
                    assertEquals("Board 2", result.get(1).getTitle());
                })
                .And("repository should be queried once", env -> verify(boardRepository).findAll())
                .Execute();
    }

    @EzScenario
    public void getBoardByIdShouldReturnBoardWhenExists() {
        Feature.New("Board Service")
                .newScenario("Get board by id returns board when exists")
                .Given("an existing board id", env -> {
                    UUID id = UUID.randomUUID();
                    Board board = new Board("Team Board", "planning board");
                    board.setId(id);

                    env.put("boardId", id);
                    when(boardRepository.findById(id)).thenReturn(Optional.of(board));
                })
                .When("getting board by id", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    BoardDTO result = boardService.getBoardById(id);
                    env.put("result", result);
                })
                .Then("board DTO should be returned", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals(id, result.getId());
                    assertEquals("Team Board", result.getTitle());
                    assertEquals("planning board", result.getDescription());
                })
                .Execute();
    }

    @EzScenario
    public void getBoardByIdShouldThrowWhenNotFound() {
        Feature.New("Board Service")
                .newScenario("Get board by id throws when board does not exist")
                .Given("a non-existing board id", env -> {
                    UUID id = UUID.randomUUID();
                    env.put("boardId", id);
                    when(boardRepository.findById(id)).thenReturn(Optional.empty());
                })
                .When("getting board by id", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> boardService.getBoardById(id));
                    env.put("error", ex);
                })
                .Then("error message should mention board not found", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertTrue(ex.getMessage().contains("Board not found with id: " + id));
                })
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldTrimTitleAndUpdateDescription() {
        Feature.New("Board Service")
                .newScenario("Update board trims title and updates fields")
                .Given("an existing board and update request", env -> {
                    UUID id = UUID.randomUUID();
                    Board board = new Board("Old Title", "Old Desc");
                    board.setId(id);
                    UpdateBoardRequest request = new UpdateBoardRequest("  New Title  ", "New Desc");

                    env.put("boardId", id);
                    env.put("request", request);

                    when(boardRepository.findById(id)).thenReturn(Optional.of(board));
                    when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));
                })
                .When("updating board", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    BoardDTO result = boardService.updateBoard(id, request);
                    env.put("result", result);
                })
                .Then("updated DTO should contain trimmed title and new description", env -> {
                    BoardDTO result = env.get("result", BoardDTO.class);
                    assertEquals("New Title", result.getTitle());
                    assertEquals("New Desc", result.getDescription());
                })
                .And("repository save should be called with trimmed title", env -> {
                    ArgumentMatcher<Board> matcher = board ->
                            "New Title".equals(board.getTitle()) &&
                                    "New Desc".equals(board.getDescription());
                    verify(boardRepository).save(argThat(matcher));
                })
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldThrowWhenTrimmedTitleIsBlank() {
        Feature.New("Board Service")
                .newScenario("Update board throws when title becomes blank after trim")
                .Given("an existing board and blank-title update request", env -> {
                    UUID id = UUID.randomUUID();
                    Board board = new Board("Old Title", "Old Desc");
                    board.setId(id);
                    UpdateBoardRequest request = new UpdateBoardRequest("   ", null);

                    env.put("boardId", id);
                    env.put("request", request);

                    when(boardRepository.findById(id)).thenReturn(Optional.of(board));
                })
                .When("updating board", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> boardService.updateBoard(id, request));
                    env.put("error", ex);
                })
                .Then("error should indicate title cannot be blank", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Board title cannot be blank", ex.getMessage());
                })
                .And("repository save should not be called", env -> verify(boardRepository, never()).save(any(Board.class)))
                .Execute();
    }

    @EzScenario
    public void updateBoardShouldThrowWhenBoardNotFound() {
        Feature.New("Board Service")
                .newScenario("Update board throws when board does not exist")
                .Given("a non-existing board id and update request", env -> {
                    UUID id = UUID.randomUUID();
                    UpdateBoardRequest request = new UpdateBoardRequest("New Title", "New Desc");

                    env.put("boardId", id);
                    env.put("request", request);
                    when(boardRepository.findById(id)).thenReturn(Optional.empty());
                })
                .When("updating board", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    UpdateBoardRequest request = env.get("request", UpdateBoardRequest.class);
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> boardService.updateBoard(id, request));
                    env.put("error", ex);
                })
                .Then("error message should mention board not found", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertTrue(ex.getMessage().contains("Board not found with id: " + id));
                })
                .Execute();
    }

    @EzScenario
    public void deleteBoardShouldDeleteWhenBoardExists() {
        Feature.New("Board Service")
                .newScenario("Delete board removes board when id exists")
                .Given("an existing board id", env -> {
                    UUID id = UUID.randomUUID();
                    env.put("boardId", id);
                    when(boardRepository.existsById(id)).thenReturn(true);
                })
                .When("deleting board", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    boardService.deleteBoard(id);
                })
                .Then("repository should delete by id", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    verify(boardRepository).deleteById(id);
                })
                .Execute();
    }

    @EzScenario
    public void deleteBoardShouldThrowWhenBoardNotFound() {
        Feature.New("Board Service")
                .newScenario("Delete board throws when id does not exist")
                .Given("a non-existing board id", env -> {
                    UUID id = UUID.randomUUID();
                    env.put("boardId", id);
                    when(boardRepository.existsById(id)).thenReturn(false);
                })
                .When("deleting board", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> boardService.deleteBoard(id));
                    env.put("error", ex);
                })
                .Then("error message should mention board not found", env -> {
                    UUID id = env.get("boardId", UUID.class);
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertTrue(ex.getMessage().contains("Board not found with id: " + id));
                })
                .And("repository delete should not be called", env -> verify(boardRepository, never()).deleteById(any(UUID.class)))
                .Execute();
    }
}
