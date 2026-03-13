package event.to.ai.backend.board.application;

import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
import event.to.ai.backend.board.application.port.out.BoardReadModel;
import event.to.ai.backend.board.application.usecase.command.ChangeBoardDescriptionUseCase;
import event.to.ai.backend.board.application.usecase.command.CreateBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.DeleteBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.RenameBoardUseCase;
import event.to.ai.backend.board.application.usecase.command.input.ChangeBoardDescriptionInput;
import event.to.ai.backend.board.application.usecase.command.input.CreateBoardInput;
import event.to.ai.backend.board.application.usecase.command.input.DeleteBoardInput;
import event.to.ai.backend.board.application.usecase.command.input.RenameBoardInput;
import event.to.ai.backend.board.application.usecase.command.output.BoardCommandOutput;
import event.to.ai.backend.board.application.usecase.query.GetBoardByIdQuery;
import event.to.ai.backend.board.application.usecase.query.GetMyBoardsQuery;
import event.to.ai.backend.board.application.usecase.query.input.GetBoardByIdInput;
import event.to.ai.backend.board.application.usecase.query.input.GetMyBoardsInput;
import event.to.ai.backend.board.application.usecase.query.output.GetBoardByIdOutput;
import event.to.ai.backend.board.application.usecase.query.output.GetMyBoardsOutput;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BoardApplicationService {

    private final CreateBoardUseCase createBoardUseCase;
    private final RenameBoardUseCase renameBoardUseCase;
    private final ChangeBoardDescriptionUseCase changeBoardDescriptionUseCase;
    private final DeleteBoardUseCase deleteBoardUseCase;
    private final GetBoardByIdQuery getBoardByIdQuery;
    private final GetMyBoardsQuery getMyBoardsQuery;

    public BoardApplicationService(CreateBoardUseCase createBoardUseCase,
                                   RenameBoardUseCase renameBoardUseCase,
                                   ChangeBoardDescriptionUseCase changeBoardDescriptionUseCase,
                                   DeleteBoardUseCase deleteBoardUseCase,
                                   GetBoardByIdQuery getBoardByIdQuery,
                                   GetMyBoardsQuery getMyBoardsQuery) {
        this.createBoardUseCase = createBoardUseCase;
        this.renameBoardUseCase = renameBoardUseCase;
        this.changeBoardDescriptionUseCase = changeBoardDescriptionUseCase;
        this.deleteBoardUseCase = deleteBoardUseCase;
        this.getBoardByIdQuery = getBoardByIdQuery;
        this.getMyBoardsQuery = getMyBoardsQuery;
    }

    public List<BoardDTO> getAllMyBoards(UUID userId) {
        GetMyBoardsInput input = new GetMyBoardsInput();
        input.setActorUserId(userId);
        GetMyBoardsOutput output = getMyBoardsQuery.execute(input);
        return output.getBoards().stream().map(this::toDTO).toList();
    }

    public BoardDTO getBoardById(UUID id) {
        return getBoardById(id, null);
    }

    public BoardDTO getBoardById(UUID id, UUID actorUserId) {
        GetBoardByIdInput input = new GetBoardByIdInput();
        input.setBoardId(id);
        input.setActorUserId(actorUserId);

        GetBoardByIdOutput output = getBoardByIdQuery.execute(input);
        if (output.getBoard() == null) {
            throw new RuntimeException("Board not found with id: " + id);
        }
        return toDTO(output.getBoard());
    }

    public BoardDTO createBoard(UUID actorUserId, CreateBoardRequest request) {
        CreateBoardInput input = new CreateBoardInput();
        input.setActorUserId(actorUserId);
        input.setTitle(request.getTitle());
        input.setDescription(request.getDescription());

        BoardCommandOutput output = createBoardUseCase.execute(input);
        return getBoardById(output.getBoardId(), actorUserId);
    }

    public BoardDTO updateBoard(UUID id, UpdateBoardRequest request) {
        if (request.getTitle() != null) {
            RenameBoardInput renameInput = new RenameBoardInput();
            renameInput.setBoardId(id);
            renameInput.setTitle(request.getTitle());
            renameBoardUseCase.execute(renameInput);
        }

        if (request.getDescription() != null) {
            ChangeBoardDescriptionInput descriptionInput = new ChangeBoardDescriptionInput();
            descriptionInput.setBoardId(id);
            descriptionInput.setDescription(request.getDescription());
            changeBoardDescriptionUseCase.execute(descriptionInput);
        }

        return getBoardById(id);
    }

    public void deleteBoard(UUID id) {
        DeleteBoardInput input = new DeleteBoardInput();
        input.setBoardId(id);
        deleteBoardUseCase.execute(input);
    }

    private BoardDTO toDTO(BoardReadModel boardReadModel) {
        return new BoardDTO(
                boardReadModel.id(),
                boardReadModel.title(),
                boardReadModel.description(),
                boardReadModel.ownerId(),
                boardReadModel.createdAt(),
                boardReadModel.updatedAt()
        );
    }
}