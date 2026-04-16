package event.to.ai.backend.board.application;

import event.to.ai.backend.board.adapter.in.web.dto.*;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardContentRepositoryPort;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.board.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoardApplicationService {

    private final BoardRepositoryPort boardRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final BoardContentRepositoryPort boardContentRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Autowired
    public BoardApplicationService(BoardRepositoryPort boardRepositoryPort,
                                   BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                   BoardContentRepositoryPort boardContentRepositoryPort,
                                   UserRepositoryPort userRepositoryPort) {
        this.boardRepositoryPort = boardRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardContentRepositoryPort = boardContentRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    public List<BoardDTO> getAllMyBoards(UUID userId) {
        return boardMembershipRepositoryPort.findAllByUserId(userId)
                                .stream()
                                .map(membership -> toDTO(membership.getBoard()))
                                .collect(Collectors.toList());
    }

    public BoardDTO getBoardById(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        Board board = boardRepositoryPort.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardId));
        return toDTO(board);
    }

    public boolean isBoardMember(UUID userId, UUID boardId) {
        return boardMembershipRepositoryPort.existsByBoardIdAndUserId(boardId, userId);
    }

    public List<BoardMembershipDTO> getBoardMembershipByBoardId(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        return boardMembershipRepositoryPort.findAllByBoardId(boardId)
                .stream()
                .map(this::toBoardMembershipDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardDTO createBoard(UUID actorUserId, CreateBoardRequest request) {
        var actor = userRepositoryPort.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + actorUserId));

        String title = request.getTitle().trim();
        if (title.isEmpty()) {
            throw new RuntimeException("Board title cannot be blank");
        }

        Board board = new Board(title, request.getDescription());
        board.setOwnerId(actorUserId);
        Board savedBoard = boardRepositoryPort.save(board);

        BoardMembership ownerMembership = new BoardMembership(savedBoard, actor, BoardMembershipRole.OWNER);
        boardMembershipRepositoryPort.save(ownerMembership);

        return toDTO(savedBoard);
    }

    @Transactional
    public BoardMembershipDTO createBoardMembership(UUID actorUserId, AddBoardMemberRequest request) {
        requireOwnerPermission(request.getBoardId(), actorUserId);

        userRepositoryPort.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + actorUserId));

        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        var targetUser = userRepositoryPort.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getUserEmail()));

        if (request.getRole() == BoardMembershipRole.OWNER) {
            throw new RuntimeException("Cannot assign OWNER role");
        }

        if (boardMembershipRepositoryPort.existsByBoardIdAndUserEmail(request.getBoardId(), request.getUserEmail())) {
            throw new RuntimeException("User is already a member of this board");
        }

        BoardMembership boardMembership = new BoardMembership(board, targetUser, request.getRole());
        BoardMembership savedBoardMember = boardMembershipRepositoryPort.save(boardMembership);
        return toBoardMembershipDTO(savedBoardMember);
    }

    @Transactional
    public BoardDTO updateBoard(UUID actorUserId, UUID id, UpdateBoardRequest request) {
        requireWritePermission(id, actorUserId);
        Board board = boardRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new RuntimeException("Board title cannot be blank");
            }
            board.setTitle(title);
        }

        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }

        Board updatedBoard = boardRepositoryPort.save(board);
        return toDTO(updatedBoard);
    }

    @Transactional
    public BoardMembershipDTO updateBoardMemeberRole(UUID actorUserId, UUID boardId, UpdateBoardMemberRoleRequest request) {
        requireOwnerPermission(boardId, actorUserId);

        if (request.getRole() == BoardMembershipRole.OWNER) {
            throw new RuntimeException("Cannot assign OWNER role");
        }

        BoardMembership membership = boardMembershipRepositoryPort
                .findByBoardIdAndUserEmail(boardId, request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("Board membership not found"));

        membership.setRole(request.getRole());
        BoardMembership updatedMembership = boardMembershipRepositoryPort.save(membership);
        return toBoardMembershipDTO(updatedMembership);
    }

    @Transactional
    public void deleteBoardMembership(UUID actorUserId, UUID boardId, UUID userId) {
        requireOwnerPermission(boardId, actorUserId);

        BoardMembership membership = boardMembershipRepositoryPort
                .findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new RuntimeException("Board membership not found"));

        if (membership.getRole() == BoardMembershipRole.OWNER) {
            throw new RuntimeException("Cannot remove the OWNER from the board");
        }

        boardMembershipRepositoryPort.deleteByBoardIdAndUserId(boardId, userId);
    }

    @Transactional
    public void deleteBoard(UUID actorUserId, UUID id) {
        requireOwnerPermission(id, actorUserId);

        if (!boardRepositoryPort.existsById(id)) {
            throw new RuntimeException("Board not found with id: " + id);
        }
        boardContentRepositoryPort.deleteAllByBoardId(id);
        boardMembershipRepositoryPort.deleteAllByBoardId(id);
        boardRepositoryPort.deleteById(id);
    }

    // 查詢發 API 的 user 是否有權限
    private BoardMembershipRole getMemberRole(UUID boardId, UUID userId) {
        return boardMembershipRepositoryPort
                .findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this board"))
                .getRole();
    }

    // 負責管理「讀」
    private void requireReadPermission(UUID boardId, UUID actorUserId) {
        getMemberRole(boardId, actorUserId);
    }

    // 負責管理「寫」
    private void requireWritePermission(UUID boardId, UUID actorUserId) {
        BoardMembershipRole role = getMemberRole(boardId, actorUserId);
        if (role == BoardMembershipRole.VIEWER) {
            throw new RuntimeException("Viewers are not allowed to perform write operations");
        }
    }

    // 負責管理「擁有者」操作
    private void requireOwnerPermission(UUID boardId, UUID actorUserId) {
        BoardMembershipRole role = getMemberRole(boardId, actorUserId);
        if (role != BoardMembershipRole.OWNER) {
            throw new RuntimeException("Only the board owner can perform this action");
        }
    }

    private BoardDTO toDTO(Board board) {
        return new BoardDTO(
                board.getId(),
                board.getTitle(),
                board.getDescription(),
                board.getOwnerId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }

    private BoardMembershipDTO toBoardMembershipDTO(BoardMembership boardMembership) {
        return new BoardMembershipDTO(
                boardMembership.getBoard().getId(),
                boardMembership.getUser().getId(),
                boardMembership.getRole(),
                boardMembership.getCreatedAt(),
                boardMembership.getUpdatedAt()
        );
    }
}
