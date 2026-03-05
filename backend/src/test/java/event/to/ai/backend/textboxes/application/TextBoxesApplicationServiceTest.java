//package event.to.ai.backend.text.application;
//
//import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
//import event.to.ai.backend.text.adapter.in.web.dto.CreateTextBoxesRequest;
//import event.to.ai.backend.text.adapter.in.web.dto.TextBoxesDTO;
//import event.to.ai.backend.text.adapter.in.web.dto.UpdateTextBoxesRequest;
//import event.to.ai.backend.text.adapter.out.persistence.entity.Point2D;
//import event.to.ai.backend.text.adapter.out.persistence.entity.TextBoxes;
//import event.to.ai.backend.text.application.port.out.BoardRepositoryPort;
//import event.to.ai.backend.text.application.port.out.TextBoxesRepositoryPort;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import tw.teddysoft.ezspec.extension.junit5.EzScenario;
//import tw.teddysoft.ezspec.keyword.Feature;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class TextBoxesApplicationServiceTest {
//
//    @Mock
//    private TextBoxesRepositoryPort textBoxesRepositoryPort;
//
//    @Mock
//    private BoardRepositoryPort boardRepositoryPort;
//
//    @InjectMocks
//    private TextBoxesApplicationService textBoxesApplicationService;
//
//    @EzScenario
//    public void getAllTextBoxesShouldOnlyReturnOwnedTextBoxes() {
//        Feature.New("TextBoxes Application Service")
//                .newScenario("Get all text boxes filters out data not owned by current user")
//                .Given("text boxes across two board owners", env -> {
//                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
//                    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
//
//                    TextBoxes ownedTextBox = createTextBox(
//                            UUID.fromString("00000000-0000-0000-0000-000000000011"),
//                            UUID.fromString("00000000-0000-0000-0000-000000000101"),
//                            actorUserId,
//                            "yellow"
//                    );
//                    TextBoxes foreignTextBox = createTextBox(
//                            UUID.fromString("00000000-0000-0000-0000-000000000012"),
//                            UUID.fromString("00000000-0000-0000-0000-000000000102"),
//                            otherUserId,
//                            "blue"
//                    );
//
//                    env.put("actorUserId", actorUserId);
//                    when(textBoxesRepositoryPort.findAll()).thenReturn(List.of(ownedTextBox, foreignTextBox));
//                })
//                .When("reading all text boxes", env -> {
//                    UUID actorUserId = env.get("actorUserId", UUID.class);
//                    List<TextBoxesDTO> result = textBoxesApplicationService.getAllTextBoxes(actorUserId);
//                    env.put("result", result);
//                })
//                .Then("only owned text boxes should be returned", env -> {
//                    List<TextBoxesDTO> result = env.get("result", List.class);
//                    assertEquals(1, result.size());
//                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000011"), result.get(0).getId());
//                })
//                .Execute();
//    }
//
//    @EzScenario
//    public void getTextBoxesByIdShouldHideForeignTextBox() {
//        Feature.New("TextBoxes Application Service")
//                .newScenario("Get text box by id returns empty when text box belongs to another user")
//                .Given("a text box owned by a different user", env -> {
//                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
//                    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
//                    UUID textBoxId = UUID.fromString("00000000-0000-0000-0000-000000000022");
//
//                    TextBoxes foreignTextBox = createTextBox(
//                            textBoxId,
//                            UUID.fromString("00000000-0000-0000-0000-000000000202"),
//                            otherUserId,
//                            "green"
//                    );
//
//                    env.put("actorUserId", actorUserId);
//                    env.put("textBoxId", textBoxId);
//                    when(textBoxesRepositoryPort.findById(textBoxId)).thenReturn(Optional.of(foreignTextBox));
//                })
//                .When("reading text box by id", env -> {
//                    UUID actorUserId = env.get("actorUserId", UUID.class);
//                    UUID textBoxId = env.get("textBoxId", UUID.class);
//                    List<TextBoxesDTO> result = textBoxesApplicationService.getTextBoxesById(actorUserId, textBoxId);
//                    env.put("result", result);
//                })
//                .Then("service should not expose foreign text box", env -> {
//                    List<TextBoxesDTO> result = env.get("result", List.class);
//                    assertTrue(result.isEmpty());
//                })
//                .Execute();
//    }
//
//    @EzScenario
//    public void createTextBoxesShouldRejectForeignBoard() {
//        Feature.New("TextBoxes Application Service")
//                .newScenario("Create text box rejects board owned by another user")
//                .Given("an existing board owned by someone else", env -> {
//                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
//                    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
//                    UUID foreignBoardId = UUID.fromString("00000000-0000-0000-0000-000000000303");
//
//                    Board foreignBoard = new Board("Foreign Board", "Description");
//                    foreignBoard.setId(foreignBoardId);
//                    foreignBoard.setOwnerId(otherUserId);
//
//                    CreateTextBoxesRequest request = new CreateTextBoxesRequest(
//                            foreignBoardId,
//                            10.0,
//                            20.0,
//                            100.0,
//                            50.0,
//                            "Demo text",
//                            "yellow",
//                            "tag"
//                    );
//
//                    env.put("actorUserId", actorUserId);
//                    env.put("request", request);
//                    when(boardRepositoryPort.findById(foreignBoardId)).thenReturn(Optional.of(foreignBoard));
//                })
//                .When("creating text box", env -> {
//                    UUID actorUserId = env.get("actorUserId", UUID.class);
//                    CreateTextBoxesRequest request = env.get("request", CreateTextBoxesRequest.class);
//
//                    RuntimeException ex = assertThrows(
//                            RuntimeException.class,
//                            () -> textBoxesApplicationService.createTextBoxes(actorUserId, request)
//                    );
//                    env.put("error", ex);
//                })
//                .Then("service should reject as forbidden", env -> {
//                    RuntimeException ex = env.get("error", RuntimeException.class);
//                    assertEquals("Forbidden", ex.getMessage());
//                    verify(textBoxesRepositoryPort, never()).save(any());
//                })
//                .Execute();
//    }
//
//    @EzScenario
//    public void updateTextBoxesShouldRejectMovingToForeignBoard() {
//        Feature.New("TextBoxes Application Service")
//                .newScenario("Update text box rejects changing board to another user's board")
//                .Given("an owned text box and a target board owned by someone else", env -> {
//                    UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
//                    UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
//                    UUID textBoxId = UUID.fromString("00000000-0000-0000-0000-000000000033");
//                    UUID foreignBoardId = UUID.fromString("00000000-0000-0000-0000-000000000404");
//
//                    TextBoxes ownedTextBox = createTextBox(
//                            textBoxId,
//                            UUID.fromString("00000000-0000-0000-0000-000000000301"),
//                            actorUserId,
//                            "pink"
//                    );
//                    Board foreignBoard = new Board("Foreign Board", "Description");
//                    foreignBoard.setId(foreignBoardId);
//                    foreignBoard.setOwnerId(otherUserId);
//
//                    UpdateTextBoxesRequest request = new UpdateTextBoxesRequest(
//                            foreignBoardId,
//                            null,
//                            null,
//                            null,
//                            null,
//                            null,
//                            null,
//                            null
//                    );
//
//                    env.put("actorUserId", actorUserId);
//                    env.put("textBoxId", textBoxId);
//                    env.put("request", request);
//                    when(textBoxesRepositoryPort.findById(textBoxId)).thenReturn(Optional.of(ownedTextBox));
//                    when(boardRepositoryPort.findById(foreignBoardId)).thenReturn(Optional.of(foreignBoard));
//                })
//                .When("updating text box board", env -> {
//                    UUID actorUserId = env.get("actorUserId", UUID.class);
//                    UUID textBoxId = env.get("textBoxId", UUID.class);
//                    UpdateTextBoxesRequest request = env.get("request", UpdateTextBoxesRequest.class);
//
//                    RuntimeException ex = assertThrows(
//                            RuntimeException.class,
//                            () -> textBoxesApplicationService.updateTextBoxes(actorUserId, textBoxId, request)
//                    );
//                    env.put("error", ex);
//                })
//                .Then("service should reject as forbidden", env -> {
//                    RuntimeException ex = env.get("error", RuntimeException.class);
//                    assertEquals("Forbidden", ex.getMessage());
//                })
//                .Execute();
//    }
//
//    private TextBoxes createTextBox(UUID textBoxId, UUID boardId, UUID ownerId, String color) {
//        Board board = new Board("Board", "Description");
//        board.setId(boardId);
//        board.setOwnerId(ownerId);
//
//        TextBoxes textBox = new TextBoxes();
//        textBox.setId(textBoxId);
//        textBox.setBoard(board);
//        textBox.setPos(new Point2D(10.0, 20.0));
//        textBox.setGeo(new Point2D(100.0, 50.0));
//        textBox.setDescription("Demo text");
//        textBox.setColor(color);
//        textBox.setTag("tag");
//        return textBox;
//    }
//}
//
