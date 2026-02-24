package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.application.port.out.UserRepositoryPort;
import event.to.ai.backend.repository.UserRepository;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    @Autowired
    public UserPersistenceAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
