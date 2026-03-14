package event.to.ai.backend.board.adapter.in.web.dto;

import event.to.ai.backend.stickynote.adapter.in.web.dto.StickyNoteDTO;
import event.to.ai.backend.textbox.adapter.in.web.dto.TextBoxesDTO;

import java.util.List;
import java.util.UUID;

public class BoardComponentsDTO {

    private UUID boardId;
    private List<StickyNoteDTO> stickyNotes;
    private List<TextBoxesDTO> textBoxes;

    public BoardComponentsDTO(UUID boardId, List<StickyNoteDTO> stickyNotes, List<TextBoxesDTO> textBoxes) {
        this.boardId = boardId;
        this.stickyNotes = stickyNotes;
        this.textBoxes = textBoxes;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public List<StickyNoteDTO> getStickyNotes() {
        return stickyNotes;
    }

    public List<TextBoxesDTO> getTextBoxes() {
        return textBoxes;
    }
}
