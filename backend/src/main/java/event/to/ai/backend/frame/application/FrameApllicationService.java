package event.to.ai.backend.frame.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.frame.adapter.in.web.dto.CreateFrameRequest;
import event.to.ai.backend.frame.adapter.in.web.dto.FrameDTO;
import event.to.ai.backend.frame.adapter.in.web.dto.UpdateFrameRequest;
import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;
import event.to.ai.backend.frame.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.frame.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.frame.application.port.out.FrameRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FrameApllicationService {

    private final FrameRepositoryPort frameRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;

    @Autowired
    public FrameApllicationService(FrameRepositoryPort frameRepositoryPort,
                                   BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                   BoardRepositoryPort boardRepositoryPort) {
        this.frameRepositoryPort = frameRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
    }

    public List<FrameDTO> getAllFrames(UUID actorUserId) {
        return frameRepositoryPort.findAll().stream()
                .filter(frame -> frame.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                frame.getBoard().getId(), actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FrameDTO> getFramesById(UUID actorUserId, UUID id) {
        return frameRepositoryPort.findById(id)
                .filter(frame -> frame.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                frame.getBoard().getId(), actorUserId))
                .map(frame -> List.of(convertToDTO(frame)))
                .orElseGet(List::of);
    }

    public List<FrameDTO> getFramesByBoardId(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        return frameRepositoryPort.findByBoardId(boardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FrameDTO createFrame(UUID actorUserId, CreateFrameRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        requireWritePermission(board.getId(), actorUserId);

        Frame frame = new Frame();
        frame.setBoard(board);
        frame.setPos(new Point2D(request.getPosX(), request.getPosY()));
        frame.setSize(new Point2D(request.getWidth(), request.getHeight()));
        frame.setTitle(request.getTitle());

        Frame savedFrame = frameRepositoryPort.save(frame);
        return convertToDTO(savedFrame);
    }

    @Transactional
    public FrameDTO updateFrame(UUID actorUserId, UUID id, UpdateFrameRequest request) {
        Frame frame = frameRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Frame not found with id: " + id));

        requireWritePermission(frame.getBoard().getId(), actorUserId);

        if (request.getBoardId() != null) {
            Board board = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            requireWritePermission(board.getId(), actorUserId);
            frame.setBoard(board);
        }

        if (request.getPosX() != null && request.getPosY() != null) {
            frame.setPos(new Point2D(request.getPosX(), request.getPosY()));
        }
        if (request.getWidth() != null && request.getHeight() != null) {
            frame.setSize(new Point2D(request.getWidth(), request.getHeight()));
        }
        if (request.getTitle() != null) {
            frame.setTitle(request.getTitle());
        }

        Frame updatedFrame = frameRepositoryPort.save(frame);
        return convertToDTO(updatedFrame);
    }

    @Transactional
    public void deleteFrame(UUID actorUserId, UUID id) {
        Frame frame = frameRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Frame not found with id: " + id));

        requireWritePermission(frame.getBoard().getId(), actorUserId);

        frameRepositoryPort.deleteById(id);
    }

    private BoardMembershipRole getMemberRole(UUID boardId, UUID actorUserId) {
        return boardMembershipRepositoryPort
                .findByBoardIdAndUserId(boardId, actorUserId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this board"))
                .getRole();
    }

    private void requireReadPermission(UUID boardId, UUID actorUserId) {
        getMemberRole(boardId, actorUserId);
    }

    private void requireWritePermission(UUID boardId, UUID actorUserId) {
        BoardMembershipRole role = getMemberRole(boardId, actorUserId);
        if (role == BoardMembershipRole.VIEWER) {
            throw new RuntimeException("Viewers are not allowed to perform write operations");
        }
    }

    private FrameDTO convertToDTO(Frame frame) {
        return new FrameDTO(
                frame.getId(),
                frame.getBoard().getId(),
                frame.getPos().getX(),
                frame.getPos().getY(),
                frame.getSize().getX(),
                frame.getSize().getY(),
                frame.getTitle()
        );
    }
}
