package event.to.ai.backend.domainmodel.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainAttributeDTO;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainEntityDTO;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.CreateDomainEntityRequest;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.UpdateDomainEntityRequest;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainAttributeData;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainEntity;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.domainmodel.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.domainmodel.application.port.out.DomainEntityRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DomainEntityApplicationService {

    private final DomainEntityRepositoryPort domainEntityRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;

    @Autowired
    public DomainEntityApplicationService(DomainEntityRepositoryPort domainEntityRepositoryPort,
                                          BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                          BoardRepositoryPort boardRepositoryPort) {
        this.domainEntityRepositoryPort = domainEntityRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
    }

    public List<DomainEntityDTO> getAllDomainEntities(UUID actorUserId) {
        return domainEntityRepositoryPort.findAll().stream()
                .filter(domainEntity -> domainEntity.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                domainEntity.getBoard().getId(), actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DomainEntityDTO> getDomainEntityById(UUID actorUserId, UUID id) {
        return domainEntityRepositoryPort.findById(id)
                .filter(domainEntity -> domainEntity.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                domainEntity.getBoard().getId(), actorUserId))
                .map(domainEntity -> List.of(convertToDTO(domainEntity)))
                .orElseGet(List::of);
    }

    public List<DomainEntityDTO> getDomainEntitiesByBoardId(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        return domainEntityRepositoryPort.findByBoardId(boardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DomainEntityDTO createDomainEntity(UUID actorUserId, CreateDomainEntityRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        requireWritePermission(board.getId(), actorUserId);

        DomainEntity domainEntity = new DomainEntity();
        domainEntity.setBoard(board);
        domainEntity.setPos(new Point2D(request.getPosX(), request.getPosY()));
        domainEntity.setSize(new Point2D(request.getWidth(), request.getHeight()));
        domainEntity.setName(request.getName());
        domainEntity.setDescription(request.getDescription());

        if (request.getAttributes() != null) {
            List<DomainAttributeData> attributes = request.getAttributes().stream()
                    .map(attr -> new DomainAttributeData(
                            attr.getName(), attr.getDataType(), attr.getConstraint(), attr.getDisplayOrder()))
                    .collect(Collectors.toList());
            domainEntity.setAttributes(attributes);
        }

        DomainEntity savedEntity = domainEntityRepositoryPort.save(domainEntity);
        return convertToDTO(savedEntity);
    }

    @Transactional
    public DomainEntityDTO updateDomainEntity(UUID actorUserId, UUID id, UpdateDomainEntityRequest request) {
        DomainEntity domainEntity = domainEntityRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("DomainEntity not found with id: " + id));

        requireWritePermission(domainEntity.getBoard().getId(), actorUserId);

        if (request.getBoardId() != null) {
            Board board = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            requireWritePermission(board.getId(), actorUserId);
            domainEntity.setBoard(board);
        }
        if (request.getPosX() != null && request.getPosY() != null) {
            domainEntity.setPos(new Point2D(request.getPosX(), request.getPosY()));
        }
        if (request.getWidth() != null && request.getHeight() != null) {
            domainEntity.setSize(new Point2D(request.getWidth(), request.getHeight()));
        }
        if (request.getName() != null) {
            domainEntity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            domainEntity.setDescription(request.getDescription());
        }
        if (request.getAttributes() != null) {
            List<DomainAttributeData> attributes = request.getAttributes().stream()
                    .map(attr -> new DomainAttributeData(
                            attr.getName(), attr.getDataType(), attr.getConstraint(), attr.getDisplayOrder()))
                    .collect(Collectors.toList());
            domainEntity.setAttributes(attributes);
        }

        DomainEntity updatedEntity = domainEntityRepositoryPort.save(domainEntity);
        return convertToDTO(updatedEntity);
    }

    @Transactional
    public void deleteDomainEntity(UUID actorUserId, UUID id) {
        DomainEntity domainEntity = domainEntityRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("DomainEntity not found with id: " + id));

        requireWritePermission(domainEntity.getBoard().getId(), actorUserId);

        domainEntityRepositoryPort.deleteById(id);
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

    private DomainEntityDTO convertToDTO(DomainEntity domainEntity) {
        List<DomainAttributeDTO> attributeDTOs = domainEntity.getAttributes().stream()
                .map(attr -> new DomainAttributeDTO(
                        attr.getName(), attr.getDataType(), attr.getConstraint(), attr.getDisplayOrder()))
                .collect(Collectors.toList());

        return new DomainEntityDTO(
                domainEntity.getId(),
                domainEntity.getBoard().getId(),
                domainEntity.getPos() != null ? domainEntity.getPos().getX() : null,
                domainEntity.getPos() != null ? domainEntity.getPos().getY() : null,
                domainEntity.getSize() != null ? domainEntity.getSize().getX() : null,
                domainEntity.getSize() != null ? domainEntity.getSize().getY() : null,
                domainEntity.getName(),
                domainEntity.getDescription(),
                attributeDTOs
        );
    }
}
