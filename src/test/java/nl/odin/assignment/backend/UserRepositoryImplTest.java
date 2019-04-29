package nl.odin.assignment.backend;

import static nl.odin.assignment.utils.randomuser.GenerateRandomUserWiremock.FILES;
import static nl.odin.assignment.utils.randomuser.GenerateRandomUserWiremock.MAPPINGS;
import static nl.odin.assignment.utils.randomuser.GenerateRandomUserWiremock.WIREMOCK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import nl.odin.assignment.User;
import nl.odin.assignment.backend.BackendException;
import nl.odin.assignment.backend.UserRepositoryImpl;
import nl.odin.assignment.utils.randomuser.GenerateRandomUserWiremock;

/**
 * Integration test using a mocked back-end.
 */
public class UserRepositoryImplTest {

    @Rule
    public GenericContainer<
            ?> wiremock =
                    new GenericContainer<>(new ImageFromDockerfile()
                            .withFileFromFile("Dockerfile",
                                    Paths.get(WIREMOCK + "/Dockerfile")
                                            .toFile())
                            .withFileFromFile("mappings",
                                    Paths.get(MAPPINGS).toFile())
                            .withFileFromFile("__files",
                                    Paths.get(FILES).toFile()))
                                            .withExposedPorts(8080);

    @Test
    public void testFindAllReturnsAllUsers() {
        // Given
        UserRepositoryImpl impl = new UserRepositoryImpl(
                "http://localhost:" + wiremock.getFirstMappedPort(), null);
        final List<User> allUsers = impl.findAll();
        assertNotNull(allUsers);
        assertEquals(GenerateRandomUserWiremock.NUMBER_OF_USERS,
                allUsers.size());
    }

    @Test
    public void testFindByNameReturnsAUserIfOneExists() {
        // Given
        UserRepositoryImpl impl = new UserRepositoryImpl(
                "http://localhost:" + wiremock.getFirstMappedPort(), null);
        final User.FirstName testKnownFirstName = new User.FirstName("heldo");
        final User.Surname testKnownSurname = new User.Surname("campos");
        final Optional<User> user =
                impl.findByName(testKnownFirstName, testKnownSurname);
        assertTrue(user.isPresent());
    }

    @Test
    public void testFindByNameReturnsEmptyIfNoneExists() {
        // Given
        UserRepositoryImpl impl = new UserRepositoryImpl(
                "http://localhost:" + wiremock.getFirstMappedPort(), null);
        final User.FirstName testKnownFirstName =
                new User.FirstName("no known");
        final User.Surname testKnownSurname = new User.Surname("user");
        final Optional<User> user =
                impl.findByName(testKnownFirstName, testKnownSurname);
        assertFalse(user.isPresent());
    }

    @Test(expected = BackendException.class)
    public void testFindAllThrowsIfConnectionRefused() {
        // Given
        UserRepositoryImpl impl =
                new UserRepositoryImpl("http://nohost:8081", null);
        impl.findAll();
    }

    @Test(expected = BackendException.class)
    public void testFindByNameThrowsIfConnectionRefused() {
        // Given
        UserRepositoryImpl impl =
                new UserRepositoryImpl("http://nohost:8081", null);
        final User.FirstName testKnownFirstName = new User.FirstName("heldo");
        final User.Surname testKnownSurname = new User.Surname("campos");
        impl.findByName(testKnownFirstName, testKnownSurname);
    }
}
