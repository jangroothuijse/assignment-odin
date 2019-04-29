package nl.odin.assignment;

import java.util.List;
import java.util.Optional;

/**
 * Provides access to users in persistence.
 */
public interface UserRepository {
    /**
     * @return all known users
     */
    List<User> findAll();

    /**
     * @param firstName the first name of a user
     * @param surname the surname of a user
     * @return a user, if a user exists given the first name and surname, empty
     *         otherwise
     */
    Optional<User> findByName(User.FirstName firstName, User.Surname surname);
}
