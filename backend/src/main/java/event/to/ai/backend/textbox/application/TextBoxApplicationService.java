package event.to.ai.backend.textbox.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.textbox.adapter.in.web.dto.CreateTextBoxesRequest;
import event.to.ai.backend.textbox.adapter.in.web.dto.TextBoxesDTO;
import event.to.ai.backend.textbox.adapter.in.web.dto.UpdateTextBoxesRequest;
import event.to.ai.backend.textbox.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.textbox.adapter.out.persistence.entity.TextBoxes;
import event.to.ai.backend.textbox.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.textbox.application.port.out.TextBoxesRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TextBoxApplicationService {

    private final TextBoxesRepositoryPort textBoxesRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;

    @Autowired
    public TextBoxApplicationService(TextBoxesRepositoryPort textBoxesRepositoryPort,
                                     BoardRepositoryPort boardRepositoryPort) {
        this.textBoxesRepositoryPort = textBoxesRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
    }

    public List<TextBoxesDTO> getAllTextBoxes(UUID actorUserId) {
        return filterAndConvert(textBoxesRepositoryPort.findAll(), actorUserId);
    }

    public List<TextBoxesDTO> getTextBoxesById(UUID actorUserId, UUID id) {
        return textBoxesRepositoryPort.findById(id)
                .filter(textBox -> isOwnedByActor(textBox, actorUserId))
                .map(textBox -> List.of(convertToDTO(textBox)))
                .orElseGet(List::of);
    }

    public List<TextBoxesDTO> getTextBoxesByBoardId(UUID actorUserId, UUID boardId) {
        return filterAndConvert(textBoxesRepositoryPort.findByBoardId(boardId), actorUserId);
    }

    private List<TextBoxesDTO> filterAndConvert(List<TextBoxes> textBoxes, UUID actorUserId) {
        return textBoxes.stream()
                .filter(textBox -> isOwnedByActor(textBox, actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean isOwnedByActor(TextBoxes textBoxes, UUID actorUserId) {
        return textBoxes.getBoard() != null && actorUserId.equals(textBoxes.getBoard().getOwnerId());
    }

    @Transactional
    public TextBoxesDTO createTextBox(UUID actorUserId, CreateTextBoxesRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        if (!board.getOwnerId().equals(actorUserId)) {
            throw new RuntimeException("Forbidden");
        }

        TextBoxes textBoxes = new TextBoxes();
        textBoxes.setBoard(board);
        textBoxes.setPos(new Point2D(request.getPosX(), request.getPosY()));
        textBoxes.setGeo(new Point2D(request.getGeoX(), request.getGeoY()));
        textBoxes.setDescription(request.getDescription());
        textBoxes.setTag(request.getTag());

        TextBoxes saved = textBoxesRepositoryPort.save(textBoxes);
        return convertToDTO(saved);
    }

    @Transactional
    public TextBoxesDTO updateTextBox(UUID actorUserId, UUID id, UpdateTextBoxesRequest request) {
        TextBoxes textBoxes = textBoxesRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("TextBox not found with id: " + id));

        if (!textBoxes.getBoard().getOwnerId().equals(actorUserId)) {
            throw new RuntimeException("Forbidden");
        }

        if (request.getBoardId() != null) {
            Board board = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            if (!board.getOwnerId().equals(actorUserId)) {
                throw new RuntimeException("Forbidden");
            }
            textBoxes.setBoard(board);
        }

        if (request.getPosX() != null && request.getPosY() != null) {
            textBoxes.setPos(new Point2D(request.getPosX(), request.getPosY()));
        }
        if (request.getGeoX() != null && request.getGeoY() != null) {
            textBoxes.setGeo(new Point2D(request.getGeoX(), request.getGeoY()));
        }
        if (request.getDescription() != null) {
            textBoxes.setDescription(request.getDescription());
        }
        if (request.getTag() != null) {
            textBoxes.setTag(request.getTag());
        }

        TextBoxes updated = textBoxesRepositoryPort.save(textBoxes);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteTextBox(UUID actorUserId, UUID id) {
        TextBoxes textBoxes = textBoxesRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("TextBox not found with id: " + id));

        if (!textBoxes.getBoard().getOwnerId().equals(actorUserId)) {
            throw new RuntimeException("Forbidden");
        }

        textBoxesRepositoryPort.deleteById(id);
    }

    private TextBoxesDTO convertToDTO(TextBoxes textBoxes) {
        return new TextBoxesDTO(
                textBoxes.getId(),
                textBoxes.getBoard().getId(),
                textBoxes.getPos().getX(),
                textBoxes.getPos().getY(),
                textBoxes.getGeo().getX(),
                textBoxes.getGeo().getY(),
                textBoxes.getDescription(),
                null,
                textBoxes.getTag()
        );
    }
}
