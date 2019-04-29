package nl.odin.assignment.utils.randomuser;

import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import nl.odin.assignment.User;

/**
 * Models the response of from randomuser.me. Omitting any fields we do not
 * need.
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Document {
    private final @NotNull List<Document.Result> results;

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Result {
        private final @NotNull Result.Gender gender;
        private final @NotNull Result.Name name;
        private final @NotNull Result.DateOfBirth dob;
        private final @NotNull Result.Picture picture;

        @Value
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static final class Name {
            private final @NotNull String first;
            private final @NotNull String last;
        }

        @Value
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static final class DateOfBirth {
            private final @NotNull ZonedDateTime date;
        }

        @Value
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static final class Picture {
            private final @NotNull String medium;
        }

        public enum Gender {
            @JsonProperty("male")
            MALE,
            @JsonProperty("female")
            FEMALE;

            /**
             * @return the User.Gender corresponding to this randomuser.me
             *         specific Gender.
             */
            public User.Gender toGender() {
                return this == MALE ? User.Gender.MALE : User.Gender.FEMALE;
            }
        }

        /**
         * @return extracted User based on this Result
         */
        public User toUser() {
            // extracts a User from this result object
            final User.FirstName firstName =
                    new User.FirstName(name.getFirst());
            final User.Surname surname = new User.Surname(name.getLast());
            return new User(firstName, surname, dob.getDate(),
                    gender.toGender(), picture.getMedium());
        }
    }
}