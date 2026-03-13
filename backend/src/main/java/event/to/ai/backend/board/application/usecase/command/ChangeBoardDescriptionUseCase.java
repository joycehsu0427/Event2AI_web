package event.to.ai.backend.board.application.usecase.command;

import event.to.ai.backend.board.application.usecase.command.input.ChangeBoardDescriptionInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import tw.teddysoft.ezddd.cqrs.usecase.command.Command;

public interface ChangeBoardDescriptionUseCase extends Command<ChangeBoardDescriptionInput, BoardCommandOutput> {
}