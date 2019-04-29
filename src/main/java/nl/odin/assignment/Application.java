package nl.odin.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Not a domain model and does not contain any business logic, exists here for
 * convenience. Springs Component scanning automatically includes any sub
 * packages of the this package.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
