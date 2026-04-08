package event.to.ai.backend.connector.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.connector.adapter.in.web.dto.ConnectorDTO;
import event.to.ai.backend.connector.adapter.in.web.dto.CreateConnectorRequest;
import event.to.ai.backend.connector.adapter.in.web.dto.UpdateConnectorRequest;
import event.to.ai.backend.connector.adapter.out.persistence.entity.Connector;
import event.to.ai.backend.connector.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.connector.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.connector.application.port.out.ConnectorRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConnectorApplicationService {

    private final ConnectorRepositoryPort connectorRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;

    @Autowired
    public ConnectorApplicationService(ConnectorRepositoryPort connectorRepositoryPort,
                                       BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                       BoardRepositoryPort boardRepositoryPort) {
        this.connectorRepositoryPort = connectorRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
    }

    public List<ConnectorDTO> getAllConnectors(UUID actorUserId) {
        return connectorRepositoryPort.findAll().stream()
                .filter(connector -> connector.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                connector.getBoard().getId(), actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ConnectorDTO> getConnectorsById(UUID actorUserId, UUID id) {
        return connectorRepositoryPort.findById(id)
                .filter(connector -> connector.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                connector.getBoard().getId(), actorUserId))
                .map(connector -> List.of(convertToDTO(connector)))
                .orElseGet(List::of);
    }

    public List<ConnectorDTO> getConnectorsByBoardId(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        return connectorRepositoryPort.findByBoardId(boardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConnectorDTO createConnector(UUID actorUserId, CreateConnectorRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        requireWritePermission(board.getId(), actorUserId);

        Connector connector = new Connector();
        connector.setBoard(board);
        connector.setFrameId(request.getFrameId());
        connector.setFromTargetType(request.getFromTargetType());
        connector.setFromTargetId(request.getFromTargetId());
        connector.setFromSide(request.getFromSide());
        connector.setFromOffset(request.getFromOffset());
        connector.setFromPoint(new Point2D(request.getFromX(), request.getFromY()));
        connector.setToTargetType(request.getToTargetType());
        connector.setToTargetId(request.getToTargetId());
        connector.setToSide(request.getToSide());
        connector.setToOffset(request.getToOffset());
        connector.setToPoint(new Point2D(request.getToX(), request.getToY()));
        connector.setLineType(request.getLineType());
        connector.setLabel(request.getLabel());
        connector.setStrokeColor(request.getStrokeColor());
        connector.setStrokeWidth(request.getStrokeWidth());
        connector.setDashed(request.getDashed());
        connector.setStartArrow(request.getStartArrow());
        connector.setEndArrow(request.getEndArrow());
        connector.setZIndex(request.getZIndex());

        Connector savedConnector = connectorRepositoryPort.save(connector);
        return convertToDTO(savedConnector);
    }

    @Transactional
    public ConnectorDTO updateConnector(UUID actorUserId, UUID id, UpdateConnectorRequest request) {
        Connector connector = connectorRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector not found with id: " + id));

        requireWritePermission(connector.getBoard().getId(), actorUserId);

        if (request.getBoardId() != null) {
            Board board = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            requireWritePermission(board.getId(), actorUserId);
            connector.setBoard(board);
        }
        if (request.getFrameId() != null) {
            if(request.getFrameId().equals("null")){
                connector.setFrameId(null);
            }
            else{
                try{
                    connector.setFrameId(UUID.fromString(request.getFrameId()));
                }catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid FrameID format: " + request.getFrameId());
                }
            }
        }
        if (request.getFromTargetType() != null) {
            connector.setFromTargetType(request.getFromTargetType());
        }
        if (request.getFromTargetId() != null) {
            connector.setFromTargetId(request.getFromTargetId());
        }
        if (request.getFromSide() != null) {
            connector.setFromSide(request.getFromSide());
        }
        if (request.getFromOffset() != null) {
            connector.setFromOffset(request.getFromOffset());
        }
        if (request.getFromX() != null && request.getFromY() != null) {
            connector.setFromPoint(new Point2D(request.getFromX(), request.getFromY()));
        }
        if (request.getToTargetType() != null) {
            connector.setToTargetType(request.getToTargetType());
        }
        if (request.getToTargetId() != null) {
            connector.setToTargetId(request.getToTargetId());
        }
        if (request.getToSide() != null) {
            connector.setToSide(request.getToSide());
        }
        if (request.getToOffset() != null) {
            connector.setToOffset(request.getToOffset());
        }
        if (request.getToX() != null && request.getToY() != null) {
            connector.setToPoint(new Point2D(request.getToX(), request.getToY()));
        }
        if (request.getLineType() != null) {
            connector.setLineType(request.getLineType());
        }
        if (request.getLabel() != null) {
            connector.setLabel(request.getLabel());
        }
        if (request.getStrokeColor() != null) {
            connector.setStrokeColor(request.getStrokeColor());
        }
        if (request.getStrokeWidth() != null) {
            connector.setStrokeWidth(request.getStrokeWidth());
        }
        if (request.getDashed() != null) {
            connector.setDashed(request.getDashed());
        }
        if (request.getStartArrow() != null) {
            connector.setStartArrow(request.getStartArrow());
        }
        if (request.getEndArrow() != null) {
            connector.setEndArrow(request.getEndArrow());
        }
        if (request.getZIndex() != null) {
            connector.setZIndex(request.getZIndex());
        }

        Connector updatedConnector = connectorRepositoryPort.save(connector);
        return convertToDTO(updatedConnector);
    }

    @Transactional
    public void deleteConnector(UUID actorUserId, UUID id) {
        Connector connector = connectorRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Connector not found with id: " + id));

        requireWritePermission(connector.getBoard().getId(), actorUserId);

        connectorRepositoryPort.deleteById(id);
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

    private ConnectorDTO convertToDTO(Connector connector) {
        return new ConnectorDTO(
                connector.getId(),
                connector.getBoard().getId(),
                connector.getFrameId(),
                connector.getFromTargetType(),
                connector.getFromTargetId(),
                connector.getFromSide(),
                connector.getFromOffset(),
                connector.getFromPoint() != null ? connector.getFromPoint().getX() : null,
                connector.getFromPoint() != null ? connector.getFromPoint().getY() : null,
                connector.getToTargetType(),
                connector.getToTargetId(),
                connector.getToSide(),
                connector.getToOffset(),
                connector.getToPoint() != null ? connector.getToPoint().getX() : null,
                connector.getToPoint() != null ? connector.getToPoint().getY() : null,
                connector.getLineType(),
                connector.getLabel(),
                connector.getStrokeColor(),
                connector.getStrokeWidth(),
                connector.isDashed(),
                connector.getStartArrow(),
                connector.getEndArrow(),
                connector.getZIndex()
        );
    }
}
