package event.to.ai.backend.board.domain;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import tw.teddysoft.ezddd.entity.ValueObject;

public record BoardId(UUID id) implements ValueObject {

    public static BoardId valueOf(UUID id){
        return new BoardId(id);
    }

    public static BoardId create(){
        return new BoardId(UUID.randomUUID());
    }

    public String toString(){
        return id.toString();
    }
}
