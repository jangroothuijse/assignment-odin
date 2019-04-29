package nl.odin.assignment.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import nl.odin.assignment.User;
import nl.odin.assignment.UserService;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/")
    public String index(//
            final @RequestParam(value = "firstName") Optional<
                    String> optionalFirstName, //
            final @RequestParam(value = "surname") Optional<
                    String> optionalSurname, //
            final Model model) {
        // Set attributes to persist input values
        optionalFirstName.ifPresent(
                firstName -> model.addAttribute("firstName", firstName));
        optionalSurname
                .ifPresent(surname -> model.addAttribute("surname", surname));

        // Get users
        final List<User> users = optionalFirstName //
                .map(User.FirstName::new) //
                // flatten to go from Optional<Optional<Pair<>>> to
                // Optional<Pair<>>
                .flatMap(firstName -> {
                    return optionalSurname //
                            .map(User.Surname::new) //
                            .map(surName -> Pair.of(firstName, surName));
                })//
                .map(pair -> {
                    // Both first name and surname are present, use findByName
                    return userService
                            .findByName(pair.getKey(), pair.getValue()) //
                            .map(Arrays::asList) // convert optional to list
                            .orElseGet(Collections::emptyList);
                }) // Either first name or surname is missing, use findAll
                .orElseGet(userService::findAll);

        // Make users available in template
        model.addAttribute("users", users);

        // Render in src/main/resources/templates/index.html
        return "index.html";
    }
}
