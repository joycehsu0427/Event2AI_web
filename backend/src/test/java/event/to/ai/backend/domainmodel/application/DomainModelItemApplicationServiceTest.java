package event.to.ai.backend.domainmodel.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.CreateDomainModelItemRequest;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainModelItemDTO;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.UpdateDomainModelItemRequest;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainModelItem;
import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.domainmodel.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.domainmodel.application.port.out.DomainModelItemRepositoryPort;
import event.to.ai.backend.domainmodel.domain.DomainModelItemType;
import event.to.ai.backend.websocket.BoardRealtimeEventType;
import event.to.ai.backend.websocket.BoardRealtimePublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainModelItemApplicationServiceTest {

    @Mock
    private DomainModelItemRepositoryPort domainModelItemRepositoryPort;

    @Mock
    private BoardMembershipRepositoryPort boardMembershipRepositoryPort;

    @Mock
    private BoardRepositoryPort boardRepositoryPort;

    @Mock
    private BoardRealtimePublisher boardRealtimePublisher;

    @InjectMocks
    private DomainModelItemApplicationService domainModelItemApplicationService;

    @Test
    void createDomainModelItemShouldPublishCreatedEvent() {
        UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID boardId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID domainModelItemId = UUID.fromString("00000000-0000-0000-0000-000000000201");

        Board board = createBoard(boardId);
        CreateDomainModelItemRequest request = new CreateDomainModelItemRequest(
                boardId,
                10.0,
                20.0,
                240.0,
                160.0,
                "Order",
                DomainModelItemType.AGGREGATE,
                "Order aggregate",
                List.of()
        );
        DomainModelItem saved = createDomainModelItem(
                domainModelItemId,
                board,
                "Order",
                DomainModelItemType.AGGREGATE,
                "Order aggregate"
        );

        when(boardRepositoryPort.findById(boardId)).thenReturn(Optional.of(board));
        when(boardMembershipRepositoryPort.findByBoardIdAndUserId(boardId, actorUserId))
                .thenReturn(Optional.of(createMembership(board, BoardMembershipRole.EDITOR)));
        when(domainModelItemRepositoryPort.save(any(DomainModelItem.class))).thenReturn(saved);

        domainModelItemApplicationService.createDomainModelItem(actorUserId, request);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(boardRealtimePublisher).publish(
                eq(BoardRealtimeEventType.DOMAIN_MODEL_ITEM_CREATED),
                eq(boardId),
                payloadCaptor.capture()
        );

        DomainModelItemDTO payload = assertInstanceOf(DomainModelItemDTO.class, payloadCaptor.getValue());
        assertEquals(domainModelItemId, payload.getId());
        assertEquals(boardId, payload.getBoardId());
        assertEquals("Order", payload.getName());
        assertEquals(DomainModelItemType.AGGREGATE, payload.getType());
    }

    @Test
    void updateDomainModelItemShouldPublishUpdatedEvent() {
        UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID boardId = UUID.fromString("00000000-0000-0000-0000-000000000102");
        UUID domainModelItemId = UUID.fromString("00000000-0000-0000-0000-000000000202");

        Board board = createBoard(boardId);
        DomainModelItem existing = createDomainModelItem(
                domainModelItemId,
                board,
                "Order",
                DomainModelItemType.AGGREGATE,
                "Order aggregate"
        );
        DomainModelItem updated = createDomainModelItem(
                domainModelItemId,
                board,
                "Payment",
                DomainModelItemType.ENTITY,
                "Payment entity"
        );
        UpdateDomainModelItemRequest request = new UpdateDomainModelItemRequest(
                null,
                null,
                null,
                null,
                "Payment",
                DomainModelItemType.ENTITY,
                "Payment entity",
                List.of()
        );

        when(domainModelItemRepositoryPort.findById(domainModelItemId)).thenReturn(Optional.of(existing));
        when(boardMembershipRepositoryPort.findByBoardIdAndUserId(boardId, actorUserId))
                .thenReturn(Optional.of(createMembership(board, BoardMembershipRole.EDITOR)));
        when(domainModelItemRepositoryPort.save(any(DomainModelItem.class))).thenReturn(updated);

        domainModelItemApplicationService.updateDomainModelItem(actorUserId, domainModelItemId, request);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(boardRealtimePublisher).publish(
                eq(BoardRealtimeEventType.DOMAIN_MODEL_ITEM_UPDATED),
                eq(boardId),
                payloadCaptor.capture()
        );

        DomainModelItemDTO payload = assertInstanceOf(DomainModelItemDTO.class, payloadCaptor.getValue());
        assertEquals(domainModelItemId, payload.getId());
        assertEquals("Payment", payload.getName());
        assertEquals(DomainModelItemType.ENTITY, payload.getType());
        assertEquals("Payment entity", payload.getDescription());
    }

    @Test
    void deleteDomainModelItemShouldPublishDeletedEvent() {
        UUID actorUserId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        UUID boardId = UUID.fromString("00000000-0000-0000-0000-000000000103");
        UUID domainModelItemId = UUID.fromString("00000000-0000-0000-0000-000000000203");

        Board board = createBoard(boardId);
        DomainModelItem domainModelItem = createDomainModelItem(
                domainModelItemId,
                board,
                "Order",
                DomainModelItemType.AGGREGATE,
                "Order aggregate"
        );

        when(domainModelItemRepositoryPort.findById(domainModelItemId)).thenReturn(Optional.of(domainModelItem));
        when(boardMembershipRepositoryPort.findByBoardIdAndUserId(boardId, actorUserId))
                .thenReturn(Optional.of(createMembership(board, BoardMembershipRole.EDITOR)));

        domainModelItemApplicationService.deleteDomainModelItem(actorUserId, domainModelItemId);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(boardRealtimePublisher).publish(
                eq(BoardRealtimeEventType.DOMAIN_MODEL_ITEM_DELETED),
                eq(boardId),
                payloadCaptor.capture()
        );

        Object payload = payloadCaptor.getValue();
        assertInstanceOf(Map.class, payload);
        assertEquals(domainModelItemId, ((Map<?, ?>) payload).get("id"));
    }

    private Board createBoard(UUID boardId) {
        Board board = new Board("Board", "Description");
        board.setId(boardId);
        return board;
    }

    private BoardMembership createMembership(Board board, BoardMembershipRole role) {
        BoardMembership membership = new BoardMembership();
        membership.setBoard(board);
        membership.setRole(role);
        return membership;
    }

    private DomainModelItem createDomainModelItem(
            UUID domainModelItemId,
            Board board,
            String name,
            DomainModelItemType type,
            String description
    ) {
        DomainModelItem domainModelItem = new DomainModelItem();
        domainModelItem.setId(domainModelItemId);
        domainModelItem.setBoard(board);
        domainModelItem.setPos(new Point2D(10.0, 20.0));
        domainModelItem.setSize(new Point2D(240.0, 160.0));
        domainModelItem.setName(name);
        domainModelItem.setType(type);
        domainModelItem.setDescription(description);
        domainModelItem.setAttributes(List.of());
        return domainModelItem;
    }
}
