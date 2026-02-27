package event.to.ai.backend.security;

import event.to.ai.backend.user.adapter.out.persistence.UserRepository;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return toPrincipal(user);
    }

    public AuthUserPrincipal loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return toPrincipal(user);
    }

    private AuthUserPrincipal toPrincipal(User user) {
        return new AuthUserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash());
    }
}
