package event.to.ai.backend.service;

import event.to.ai.backend.dto.BoardDTO;
import event.to.ai.backend.dto.CreateBoardRequest;
import event.to.ai.backend.dto.UpdateBoardRequest;
import event.to.ai.backend.entity.Board;
import event.to.ai.backend.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public List<BoardDTO> getAllBoards() {
        return boardRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BoardDTO getBoardById(UUID id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));
        return convertToDTO(board);
    }

    @Transactional
    public BoardDTO createBoard(CreateBoardRequest request) {
        Board board = new Board(request.getTitle().trim(), request.getDescription());
        Board savedBoard = boardRepository.save(board);
        return convertToDTO(savedBoard);
    }

    @Transactional
    public BoardDTO updateBoard(UUID id, UpdateBoardRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new RuntimeException("Board title cannot be blank");
            }
            board.setTitle(title);
        }

        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }

        Board updatedBoard = boardRepository.save(board);
        return convertToDTO(updatedBoard);
    }

    @Transactional
    public void deleteBoard(UUID id) {
        if (!boardRepository.existsById(id)) {
            throw new RuntimeException("Board not found with id: " + id);
        }
        boardRepository.deleteById(id);
    }

    private BoardDTO convertToDTO(Board board) {
        return new BoardDTO(
                board.getId(),
                board.getTitle(),
                board.getDescription(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
