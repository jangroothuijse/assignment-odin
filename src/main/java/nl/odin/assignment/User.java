package nl.odin.assignment;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotNull;

import lombok.Value;
import lombok.experimental.Wither;

/**
 * Domain model, representing a user.
 */
@Value
public final class User {
    private final @NotNull FirstName firstName;
    private final @NotNull Surname surname;
    private final @NotNull ZonedDateTime dateOfBirth;
    private final @NotNull Gender gender;
    @Wither
    private final @NotNull String pictureFileName;

    /**
     * Adds stronger typing to the first name by wrapping it value.
     * 
     * @see nl.odin.assignment.UserService for example
     */
    @Value
    public static class FirstName {
        private final @NotNull String value;
    }

    /**
     * Adds stronger typing to the surname by wrapping it value.
     * 
     * @see nl.odin.assignment.UserService for example
     */
    @Value
    public static class Surname {
        private final @NotNull String value;
    }

    public enum Gender {
        MALE,
        FEMALE;
    }
}
