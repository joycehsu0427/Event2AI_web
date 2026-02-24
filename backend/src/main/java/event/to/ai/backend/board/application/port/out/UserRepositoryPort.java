package event.to.ai.backend.board.application.port.out;

import event.to.ai.backend.user.adapter.out.persistence.entity.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findById(Long id);
}
