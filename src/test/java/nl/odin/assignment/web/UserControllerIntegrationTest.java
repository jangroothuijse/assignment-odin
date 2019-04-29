package nl.odin.assignment.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import nl.odin.assignment.User;
import nl.odin.assignment.UserService;
import nl.odin.assignment.User.Gender;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * Integration test of the user controller and the thymeleaf template.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @Test
    public void checkThatTemplateRendersWithoutErrors() throws Exception {
        // Given a user
        final User.FirstName firstName = new User.FirstName("john");
        final User.Surname surname = new User.Surname("doe");
        final User user = new User(//
                firstName, //
                surname, //
                ZonedDateTime.now(), //
                Gender.MALE, "");
        // Given that the user service will return a list with the user
        when(service.findAll()).thenReturn(Arrays.asList(user));

        // When we get all users via spring mvc
        mockMvc.perform(get("/"))
                // We expect to see a 200, ok status
                .andExpect(status().isOk())
                // and we expect john to be the result
                .andExpect(content().string(containsString("john")))
                // and we expect doe to be the result
                .andExpect(content().string(containsString("doe")));
    }
}
