package event.to.ai.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.repository.BoardRepository;
import event.to.ai.backend.repository.UserRepository;
import event.to.ai.backend.security.JwtService;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class BoardApiIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanUp() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @EzScenario
    public void createBoardShouldRequireAuthentication() {
        Feature.New("Board API Integration")
                .newScenario("Create board endpoint requires authentication")
                .When("posting board without JWT", env -> {
                    try {
                        MvcResult result = mockMvc.perform(post("/api/boards")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"title\":\"Team Board\",\"description\":\"planning\"}"))
                                .andReturn();
                        env.put("status", result.getResponse().getStatus());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .Then("response status should be unauthorized or forbidden", env -> {
                    Integer status = env.get("status", Integer.class);
                    assertTrue(status == 401 || status == 403);
                })
                .Execute();
    }

    @EzScenario
    public void createAndGetBoardShouldWorkWithValidJwtAndPersistOwnerId() {
        Feature.New("Board API Integration")
                .newScenario("Create and get board with valid JWT persists owner id")
                .Given("an existing user and valid JWT token", env -> {
                    User user = new User("alice", "alice@example.com", passwordEncoder.encode("mySecret123"));
                    user = userRepository.save(user);
                    String token = jwtService.generateToken(user.getId());
                    env.put("userId", user.getId());
                    env.put("token", token);
                })
                .When("creating board via API and fetching it back", env -> {
                    Long userId = env.get("userId", Long.class);
                    String token = env.get("token", String.class);
                    try {
                        MvcResult createResult = mockMvc.perform(post("/api/boards")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"title\":\"  Team Board  \",\"description\":\"planning\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value("Team Board"))
                                .andExpect(jsonPath("$.ownerUserId").value(userId))
                                .andReturn();

                        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
                        UUID boardId = UUID.fromString(created.get("id").asText());
                        env.put("boardId", boardId);

                        mockMvc.perform(get("/api/boards/{id}", boardId)
                                        .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(boardId.toString()))
                                .andExpect(jsonPath("$.ownerUserId").value(userId));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .Then("board should be persisted with user owner id", env -> {
                    Long userId = env.get("userId", Long.class);
                    UUID boardId = env.get("boardId", UUID.class);
                    Board persisted = boardRepository.findById(boardId).orElseThrow();
                    assertEquals(userId, persisted.getOwnerId());
                    assertEquals("Team Board", persisted.getTitle());
                })
                .Execute();
    }

    @EzScenario
    public void authLoginEndpointShouldBeAccessibleWithoutJwt() {
        Feature.New("Board API Integration")
                .newScenario("Auth login endpoint is publicly accessible without JWT")
                .When("posting login request without JWT", env -> {
                    try {
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"username\":\"nobody\",\"password\":\"mySecret123\"}"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(content().string("Invalid username or password"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .Then("request should reach auth controller and return 401 message", env -> {
                    assertEquals(0L, userRepository.count());
                    assertEquals(0L, boardRepository.count());
                })
                .Execute();
    }
}
