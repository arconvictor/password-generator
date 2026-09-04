package de.victorarcon.pwdgenerator.control;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Signals that an operation on a user's database credentials failed —
 * either the user could not be found, or the {@code ALTER USER} statement failed.
 *
 * Wrapped in a {@link Failure} rather than thrown, as part of this project's
 * functional (non-exception-based) error handling.
 */
public class DbSecretException extends RuntimeException {

    private DbSecretException(String message, Throwable exception) {
        super(message, exception);
    }

    /**
     * Creates a {@code DbSecretException} for a given user/secret, wrapping the underlying cause.
     *
     * @param secretName the username the failure relates to
     * @param throwable the underlying cause (e.g. a {@link java.sql.SQLException})
     * @return a new {@code DbSecretException} with a message identifying the affected user
     */
    public static DbSecretException forSecret(String secretName, Throwable throwable) {
        return new DbSecretException("An exception happened while handling: " + secretName, throwable);
    }

    /**
     * Equality is based on the underlying cause's type and message, so that two exceptions
     * arising from the same kind of failure (e.g. two "user not found" errors) compare equal
     * regardless of stack trace — useful when asserting on failures in tests.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getCause());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbSecretException exception)) return false;
        if (this.getCause() == null || exception.getCause() == null) return false;
        return Objects.equals(this.getCause().getClass(), exception.getCause().getClass()) && Objects.equals(this.getCause().getMessage(), exception.getCause().getMessage());
    }

    @Override
    public String toString() {
        return """
                DbSecretException{
                 message: %s,
                 cause: %s,
                 stackTrace: %s
                }""".formatted(getMessage(), getCause(), Arrays.stream(getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
    }

}
