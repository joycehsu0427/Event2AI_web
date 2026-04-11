package event.to.ai.backend.board.adapter.in.web.dto;

import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainModelItemDTO;
import event.to.ai.backend.frame.adapter.in.web.dto.FrameDTO;
import event.to.ai.backend.stickynote.adapter.in.web.dto.StickyNoteDTO;
import event.to.ai.backend.textbox.adapter.in.web.dto.TextBoxesDTO;

import java.util.List;
import java.util.UUID;

public class BoardComponentsDTO {

    private UUID boardId;
    private List<StickyNoteDTO> stickyNotes;
    private List<TextBoxesDTO> textBoxes;
    private List<FrameDTO> frames;
    private List<DomainModelItemDTO> domainModelItems;

    public BoardComponentsDTO(UUID boardId, List<StickyNoteDTO> stickyNotes, List<TextBoxesDTO> textBoxes,
                              List<FrameDTO> frames, List<DomainModelItemDTO> domainModelItems) {
        this.boardId = boardId;
        this.stickyNotes = stickyNotes;
        this.textBoxes = textBoxes;
        this.frames = frames;
        this.domainModelItems = domainModelItems;
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

    public List<FrameDTO> getFrames() {
        return frames;
    }

    public List<DomainModelItemDTO> getDomainModelItems() {
        return domainModelItems;
    }
}
