package event.to.ai.backend.board.domain;

import tw.teddysoft.ezddd.entity.EsAggregateRoot;

import java.time.LocalDateTime;
import java.util.UUID;

public class Board extends EsAggregateRoot<BoardId, BoardEvent> {
    private BoardId boardId;
    private BoardTitle boardTitle;
    private String description;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    protected void when(BoardEvent e){

    }

    @Override
    protected void ensureInvariant(){

    }

    @Override
    public BoardId getId(){
        return boardId;
    }

    @Override public String getCategory() {
        return "board";
    }

    public static Board create(BoardId id, BoardTitle title, String description, UUID ownerId) {
        Board board = new Board();
        board.apply(new BoardEvent.BoardCreated(id, title, description, ownerId));
        return board;
    }

    public void rename(String title){
//        apply(new BoardRenamed(...));
    }

    public void delete(){

    }
}
