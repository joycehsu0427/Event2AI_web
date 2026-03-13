package event.to.ai.backend.board.application.usecase.command;

import event.to.ai.backend.board.application.usecase.command.input.RenameBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import tw.teddysoft.ezddd.cqrs.usecase.command.Command;

public interface RenameBoardUseCase extends Command<RenameBoardInput, BoardCommandOutput> {
}