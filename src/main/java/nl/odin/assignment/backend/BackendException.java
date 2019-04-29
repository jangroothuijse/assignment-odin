package nl.odin.assignment.backend;

public class BackendException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    BackendException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
