package nl.odin.assignment;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import nl.odin.assignment.User.Gender;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testThatFindAllGetsForwarded() {
        // Given a list of users
        final List<User> users = new ArrayList<>();
        // Given a repository that returns the list when finding all
        when(userRepository.findAll()).thenReturn(users);

        // When we find all on the service
        final List<User> result = userService.findAll();

        // We expect the service to return the same list as the repository
        assertEquals(users, result);
    }

    @Test
    public void testThatFindByNameGetsForwarded() {
        // Given a user
        final User.FirstName firstName = new User.FirstName("john");
        final User.Surname surname = new User.Surname("doe");
        final User user = new User(//
                firstName, //
                surname, //
                ZonedDateTime.now(), //
                Gender.MALE, "");

        // Given a repository that returns the user when we find by name
        when(userRepository.findByName(firstName, surname)) //
                .thenReturn(Optional.of(user));

        // When we find by name on the service
        Optional<User> result = userService.findByName(firstName, surname);

        // We expect the same user to be returned
        assertEquals(user, result.get());
    }
}
