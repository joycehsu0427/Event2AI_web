package event.to.ai.backend.user.application.port.out;

import event.to.ai.backend.user.adapter.out.persistence.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);

    void deleteById(Long id);
}
