package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.domain.BoardEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import tw.teddysoft.ezddd.entity.DomainEventTypeMapper;
import tw.teddysoft.ezddd.usecase.port.inout.domainevent.DomainEventMapper;

@Configuration
public class BoardDomainEventMapperConfig {

    private static final String BOARD_CREATED = "BoardCreated";
    private static final String BOARD_RENAMED = "BoardRenamed";
    private static final String BOARD_DESCRIPTION_CHANGED = "BoardDescriptionChanged";
    private static final String BOARD_DELETED = "BoardDeleted";

    @PostConstruct
    public void configureDomainEventMapper() {
        DomainEventTypeMapper mapper = DomainEventTypeMapper.create();
        mapper.put(BOARD_CREATED, BoardEvent.BoardCreated.class);
        mapper.put(BOARD_RENAMED, BoardEvent.BoardRenamed.class);
        mapper.put(BOARD_DESCRIPTION_CHANGED, BoardEvent.BoardDescriptionChanged.class);
        mapper.put(BOARD_DELETED, BoardEvent.BoardDeleted.class);
        DomainEventMapper.setMapper(mapper);
    }
}
