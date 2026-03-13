package event.to.ai.backend.board.application.usecase.command;

import event.to.ai.backend.board.application.usecase.command.input.DeleteBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import tw.teddysoft.ezddd.cqrs.usecase.command.Command;

public interface DeleteBoardUseCase extends Command<DeleteBoardInput, BoardCommandOutput> {
}