package event.to.ai.backend.domainmodel.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.CreateDomainModelItemRequest;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainAttributeDTO;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainModelItemDTO;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.UpdateDomainModelItemRequest;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainAttributeData;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainModelItem;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.domainmodel.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.domainmodel.application.port.out.DomainModelItemRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DomainModelItemApplicationService {

    private final DomainModelItemRepositoryPort domainModelItemRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;

    @Autowired
    public DomainModelItemApplicationService(DomainModelItemRepositoryPort domainModelItemRepositoryPort,
                                             BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                             BoardRepositoryPort boardRepositoryPort) {
        this.domainModelItemRepositoryPort = domainModelItemRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
    }

    public List<DomainModelItemDTO> getAllDomainModelItems(UUID actorUserId) {
        return domainModelItemRepositoryPort.findAll().stream()
                .filter(domainModelItem -> domainModelItem.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                domainModelItem.getBoard().getId(), actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DomainModelItemDTO> getDomainModelItemById(UUID actorUserId, UUID id) {
        return domainModelItemRepositoryPort.findById(id)
                .filter(domainModelItem -> domainModelItem.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                domainModelItem.getBoard().getId(), actorUserId))
                .map(domainModelItem -> List.of(convertToDTO(domainModelItem)))
                .orElseGet(List::of);
    }

    public List<DomainModelItemDTO> getDomainModelItemsByBoardId(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        return domainModelItemRepositoryPort.findByBoardId(boardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DomainModelItemDTO createDomainModelItem(UUID actorUserId, CreateDomainModelItemRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        requireWritePermission(board.getId(), actorUserId);

        DomainModelItem domainModelItem = new DomainModelItem();
        domainModelItem.setBoard(board);
        domainModelItem.setPos(new Point2D(request.getPosX(), request.getPosY()));
        domainModelItem.setSize(new Point2D(request.getWidth(), request.getHeight()));
        domainModelItem.setName(request.getName());
        domainModelItem.setDescription(request.getDescription());

        if (request.getAttributes() != null) {
            List<DomainAttributeData> attributes = request.getAttributes().stream()
                    .map(attr -> new DomainAttributeData(
                            attr.getName(), attr.getDataType(), attr.getConstraint(), attr.getDisplayOrder()))
                    .collect(Collectors.toList());
            domainModelItem.setAttributes(attributes);
        }

        DomainModelItem savedDomainModelItem = domainModelItemRepositoryPort.save(domainModelItem);
        return convertToDTO(savedDomainModelItem);
    }

    @Transactional
    public DomainModelItemDTO updateDomainModelItem(UUID actorUserId, UUID id, UpdateDomainModelItemRequest request) {
        DomainModelItem domainModelItem = domainModelItemRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("DomainModelItem not found with id: " + id));

        requireWritePermission(domainModelItem.getBoard().getId(), actorUserId);

        if (request.getBoardId() != null) {
            Board board = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            requireWritePermission(board.getId(), actorUserId);
            domainModelItem.setBoard(board);
        }
        if (request.getPosX() != null && request.getPosY() != null) {
            domainModelItem.setPos(new Point2D(request.getPosX(), request.getPosY()));
        }
        if (request.getWidth() != null && request.getHeight() != null) {
            domainModelItem.setSize(new Point2D(request.getWidth(), request.getHeight()));
        }
        if (request.getName() != null) {
            domainModelItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            domainModelItem.setDescription(request.getDescription());
        }
        if (request.getAttributes() != null) {
            List<DomainAttributeData> attributes = request.getAttributes().stream()
                    .map(attr -> new DomainAttributeData(
                            attr.getName(), attr.getDataType(), attr.getConstraint(), attr.getDisplayOrder()))
                    .collect(Collectors.toList());
            domainModelItem.setAttributes(attributes);
        }

        DomainModelItem updatedDomainModelItem = domainModelItemRepositoryPort.save(domainModelItem);
        return convertToDTO(updatedDomainModelItem);
    }

    @Transactional
    public void deleteDomainModelItem(UUID actorUserId, UUID id) {
        DomainModelItem domainModelItem = domainModelItemRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("DomainModelItem not found with id: " + id));

        requireWritePermission(domainModelItem.getBoard().getId(), actorUserId);

        domainModelItemRepositoryPort.deleteById(id);
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

    private DomainModelItemDTO convertToDTO(DomainModelItem domainModelItem) {
        List<DomainAttributeDTO> attributeDTOs = domainModelItem.getAttributes().stream()
                .map(attr -> new DomainAttributeDTO(
                        attr.getName(), attr.getDataType(), attr.getConstraint(), attr.getDisplayOrder()))
                .collect(Collectors.toList());

        return new DomainModelItemDTO(
                domainModelItem.getId(),
                domainModelItem.getBoard().getId(),
                domainModelItem.getPos() != null ? domainModelItem.getPos().getX() : null,
                domainModelItem.getPos() != null ? domainModelItem.getPos().getY() : null,
                domainModelItem.getSize() != null ? domainModelItem.getSize().getX() : null,
                domainModelItem.getSize() != null ? domainModelItem.getSize().getY() : null,
                domainModelItem.getName(),
                domainModelItem.getDescription(),
                attributeDTOs
        );
    }
}
