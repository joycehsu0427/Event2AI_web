package event.to.ai.backend.analysis.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import event.to.ai.backend.analysis.adapter.in.web.dto.GroupDTO;
import event.to.ai.backend.analysis.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.analysis.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.analysis.application.port.out.FrameRepositoryPort;
import event.to.ai.backend.analysis.application.port.out.StickyNoteRepositoryPort;
import event.to.ai.backend.analysis.application.port.out.TextBoxRepositoryPort;
import event.to.ai.backend.analysis.domain.Group;
import event.to.ai.backend.analysis.domain.StickyNote;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;
import event.to.ai.backend.textbox.adapter.out.persistence.entity.TextBoxes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalysisApplicationService {

    private final StickyNoteRepositoryPort stickyNoteRepositoryPort;
    private final TextBoxRepositoryPort textBoxRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final FrameRepositoryPort frameRepositoryPort;

    @Autowired
    public AnalysisApplicationService(StickyNoteRepositoryPort stickyNoteRepositoryPort,
                                      TextBoxRepositoryPort textBoxRepositoryPort,
                                      BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                      BoardRepositoryPort boardRepositoryPort,
                                      FrameRepositoryPort frameRepositoryPort) {
        this.stickyNoteRepositoryPort = stickyNoteRepositoryPort;
        this.textBoxRepositoryPort = textBoxRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
        this.frameRepositoryPort = frameRepositoryPort;
    }

    private static final String OUTPUT_DIR = "ToAIJsonFile";

    public void analyse(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);

        List<StickyNote> allDomainNotes = new ArrayList<>();

        // 將指定 Board 上的 StickyNote 從 DB 中撈出來，加進 allDomainNotes
        stickyNoteRepositoryPort.findByBoardId(boardId).stream()
                .map(this::toDomainStickyNote)
                .forEach(allDomainNotes::add);

        // 將指定 Board 上的 TextBox 從 DB 中撈出來 (TextBox 先忽略)
//        textBoxRepositoryPort.findByBoardId(boardId).stream()
//                .map(this::textBoxToDomainStickyNote)
//                .forEach(allDomainNotes::add);

        // 建立 frameId -> frame size 的對應表
        Map<String, Point2D> frameSizes = frameRepositoryPort.findByBoardId(boardId).stream()
                .collect(Collectors.toMap(
                        frame -> frame.getId().toString(),
                        frame -> new Point2D.Double(frame.getSize().getX(), frame.getSize().getY())
                ));

        // 將撈出來的東西用 frameId 分群
        ClusterByFrameIdUseCase clusterUseCase = new ClusterByFrameIdUseCase();
        clusterUseCase.cluster(allDomainNotes);

        // 丟進 ClassifyStickNotesUseCase 中進行分類
        ClassifyStickNotesUseCase classifyUseCase = new ClassifyStickNotesUseCase();
        classifyUseCase.classify(clusterUseCase.getAllGroups(), frameSizes);

        List<GroupDTO> groups = classifyUseCase.getGroups().stream()
                .map(this::toGroupDTO)
                .collect(Collectors.toList());

        saveToJsonFile(boardId, groups);
    }

    private void saveToJsonFile(UUID boardId, List<GroupDTO> groups) {
        try {
            Board board = boardRepositoryPort.findById(boardId)
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardId));
            String boardName = board.getTitle();
            File boardDir = new File(OUTPUT_DIR, boardName);
            if (!boardDir.exists()) {
                boardDir.mkdirs();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            for (GroupDTO group : groups) {
                File outputFile = new File(boardDir, group.getUseCaseName() + ".json");
                objectMapper.writeValue(outputFile, group);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save analysis result to JSON file", e);
        }
    }

    private StickyNote toDomainStickyNote(
            event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote note) {
        String frameId = note.getFrameId() != null ? note.getFrameId().toString() : null;
        return new StickyNote(
                note.getId().toString(),
                note.getDescription(),
                new Point2D.Double(note.getPos().getX(), note.getPos().getY()),
                new Point2D.Double(note.getGeo().getX(), note.getGeo().getY()),
                note.getColor(),
                note.getTag(),
                frameId
        );
    }

    private StickyNote textBoxToDomainStickyNote(TextBoxes textBox) {
        String frameId = textBox.getFrameID() != null ? textBox.getFrameID().toString() : null;
        return new StickyNote(
                textBox.getId().toString(),
                textBox.getDescription(),
                new Point2D.Double(textBox.getPos().getX(), textBox.getPos().getY()),
                new Point2D.Double(textBox.getGeo().getX(), textBox.getGeo().getY()),
                textBox.getTag(),   // TextBox 沒有 color，用 tag 代替供 ClassifyStickNotesUseCase 識別
                textBox.getTag(),
                frameId
        );
    }

    private GroupDTO toGroupDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setGroupId(group.getGroupId());
        dto.setUseCaseName(group.getUseCaseName());
        dto.setAggregateName(group.getAggregateName());
        dto.setMethod(group.getMethod());
        dto.setActor(group.getActor());
        dto.setComment(group.getComment());
        dto.setInput(group.getInput());
        dto.setDomainEvents(group.getPublishEvents());
        dto.setAggregateWithAttributes(group.getAggregateWithAttributes());
        return dto;
    }

    private void requireReadPermission(UUID boardId, UUID actorUserId) {
        boardMembershipRepositoryPort
                .findByBoardIdAndUserId(boardId, actorUserId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this board"));
    }
}
