package event.to.ai.backend.board.application.usecase.command;

import event.to.ai.backend.board.application.usecase.command.input.CreateBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import tw.teddysoft.ezddd.cqrs.usecase.command.Command;

public interface CreateBoardUseCase extends Command<CreateBoardInput, BoardCommandOutput> {
}