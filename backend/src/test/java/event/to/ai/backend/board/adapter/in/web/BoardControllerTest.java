package event.to.ai.backend.board.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.board.application.BoardApplicationService;
import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {

    @Mock
    private BoardApplicationService boardApplicationService;

    @Mock
    private CurrentUserIdProvider currentUserIdProvider;

    @InjectMocks
    private BoardController boardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(boardController).build();
        objectMapper = new ObjectMapper();
    }

    @EzScenario
    public void createBoardShouldUseCurrentUserIdAndReturnCreated() {
        Feature.New("Board Controller")
                .newScenario("Create board uses current user id and returns 201")
                .Given("a valid request and authenticated actor id", env -> {
                    Long actorUserId = 11L;
                    CreateBoardRequest request = new CreateBoardRequest("Team Board", "planning");
                    BoardDTO response = new BoardDTO(UUID.randomUUID(), "Team Board", "planning", actorUserId, null, null);
                    env.put("actorUserId", actorUserId);
                    env.put("request", request);

                    when(currentUserIdProvider.getCurrentUserId()).thenReturn(actorUserId);
                    when(boardApplicationService.createBoard(eq(actorUserId), any(CreateBoardRequest.class))).thenReturn(response);
                })
                .When("creating board via POST /api/boards", env -> {
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    try {
                        MvcResult result = mockMvc.perform(post("/api/boards")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value("Team Board"))
                                .andExpect(jsonPath("$.ownerUserId").value(11))
                                .andReturn();
                        env.put("result", result);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .Then("controller should call provider and service with actor id", env -> {
                    Long actorUserId = env.get("actorUserId", Long.class);
                    verify(currentUserIdProvider).getCurrentUserId();
                    verify(boardApplicationService).createBoard(eq(actorUserId), any(CreateBoardRequest.class));
                })
                .Execute();
    }

    @EzScenario
    public void createBoardShouldReturnBadRequestWhenServiceThrows() {
        Feature.New("Board Controller")
                .newScenario("Create board returns 400 when service throws runtime exception")
                .Given("a valid request but service throws validation error", env -> {
                    Long actorUserId = 11L;
                    CreateBoardRequest request = new CreateBoardRequest("Team Board", "planning");
                    env.put("request", request);
                    when(currentUserIdProvider.getCurrentUserId()).thenReturn(actorUserId);
                    when(boardApplicationService.createBoard(eq(actorUserId), any(CreateBoardRequest.class)))
                            .thenThrow(new RuntimeException("Board title cannot be blank"));
                })
                .When("calling create board endpoint", env -> {
                    CreateBoardRequest request = env.get("request", CreateBoardRequest.class);
                    try {
                        mockMvc.perform(post("/api/boards")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Board title cannot be blank"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .Then("error body should be returned from controller", env -> {
                    verify(currentUserIdProvider).getCurrentUserId();
                    verify(boardApplicationService).createBoard(eq(11L), any(CreateBoardRequest.class));
                })
                .Execute();
    }

    @EzScenario
    public void getBoardByIdShouldReturnNotFoundWhenServiceThrows() {
        Feature.New("Board Controller")
                .newScenario("Get board by id returns 404 when board missing")
                .Given("a board id not found by service", env -> {
                    UUID boardId = UUID.randomUUID();
                    env.put("boardId", boardId);
                    when(boardApplicationService.getBoardById(boardId))
                            .thenThrow(new RuntimeException("Board not found with id: " + boardId));
                })
                .When("calling GET /api/boards/{id}", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    try {
                        mockMvc.perform(get("/api/boards/{id}", boardId))
                                .andExpect(status().isNotFound());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .Then("controller should map exception to 404", env -> {
                    UUID boardId = env.get("boardId", UUID.class);
                    verify(boardApplicationService).getBoardById(boardId);
                })
                .Execute();
    }
}
