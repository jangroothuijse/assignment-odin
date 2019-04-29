package nl.odin.assignment;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

@Component
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    /**
     * The service has-a repository, instead of is-a repository.
     * 
     * Generates methods that forward calls to the repository, which happen to
     * implement UserService.
     */
    @Delegate(types = UserRepository.class)
    private final UserRepository userRepository;
}
