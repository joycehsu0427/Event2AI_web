package event.to.ai.backend.board.application.usecase.command.input;

import tw.teddysoft.ezddd.usecase.port.in.interactor.Input;

import java.util.UUID;

public class CreateBoardInput implements Input {

    private UUID actorUserId;
    private String title;
    private String description;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}