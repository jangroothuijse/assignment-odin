package nl.odin.assignment.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import nl.odin.assignment.User;
import nl.odin.assignment.UserService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit test for the user controller
 */
public class UserControllerTest {
    @Mock
    private UserService userService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private UserController userController;

    @SuppressWarnings("unchecked")
    @Test
    public void testIndexCallsFindAllForPresentPresent() {
        final User.FirstName firstName = new User.FirstName("john");
        final User.Surname surname = new User.Surname("doe");

        final Model model = new ConcurrentModel();
        when(userService.findByName(firstName, surname))
                .thenReturn(Optional.empty());

        final String result = userController.index( //
                Optional.of(firstName.getValue()), //
                Optional.of(surname.getValue()), //
                model);

        assertEquals("index.html", result);
        assertEquals(firstName.getValue(), model.asMap().get("firstName"));
        assertEquals(surname.getValue(), model.asMap().get("surname"));
        assertEquals(0, ((List<User>) model.asMap().get("users")).size());
    }

    @Test
    public void testIndexCallsFindAllForEmptyEmpty() {

        final Model model = new ConcurrentModel();
        final List<User> users = new ArrayList<>();
        when(userService.findAll()).thenReturn(users);

        final String result =
                userController.index(Optional.empty(), Optional.empty(), model);

        assertEquals("index.html", result);
        assertEquals(model.asMap().get("users"), users);
    }

    @Test
    public void testIndexCallsFindAllForPresentEmpty() {
        final Model model = new ConcurrentModel();
        final List<User> users = new ArrayList<>();
        when(userService.findAll()).thenReturn(users);

        final String result = userController.index(Optional.of("john"),
                Optional.empty(), model);

        assertEquals("index.html", result);
        assertEquals("john", model.asMap().get("firstName"));
        assertEquals(model.asMap().get("users"), users);
    }

    @Test
    public void testIndexCallsFindAllForEmptyPresent() {
        final Model model = new ConcurrentModel();
        final List<User> users = new ArrayList<>();
        when(userService.findAll()).thenReturn(users);

        final String result = userController.index(Optional.empty(),
                Optional.of("doe"), model);

        assertEquals("index.html", result);
        assertEquals("doe", model.asMap().get("surname"));
        assertEquals(model.asMap().get("users"), users);
    }

}
