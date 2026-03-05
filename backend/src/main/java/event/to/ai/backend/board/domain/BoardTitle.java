package event.to.ai.backend.board.domain;

import tw.teddysoft.ezddd.entity.ValueObject;

public record BoardTitle(String title) implements ValueObject {

    public static BoardTitle valueOf(String text){
        return new BoardTitle(text);
    }

    public static BoardTitle create(){
        return new BoardTitle("");
    }

    public String toString(){
        return title;
    }
}
