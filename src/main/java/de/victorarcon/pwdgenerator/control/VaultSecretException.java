package de.victorarcon.pwdgenerator.control;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Signals that writing a secret to Vault failed for a given user.
 *
 * Wrapped in a {@link Failure} rather than thrown, mirroring {@link DbSecretException}
 * for the Vault side of the DB+Vault update.
 */
public final class VaultSecretException extends RuntimeException {

    private VaultSecretException(String message, Throwable exception) {
        super(message, exception);
    }

    /**
     * Creates a {@code VaultSecretException} for a given user/secret, wrapping the underlying cause.
     *
     * @param secretName the username/secret the failure relates to
     * @param throwable the underlying cause (e.g. a Vault client exception)
     * @return a new {@code VaultSecretException} with a message identifying the affected secret
     */
    public static VaultSecretException forSecret(String secretName, Throwable throwable) {
        return new VaultSecretException("An exception happened while handling: " + secretName, throwable);
    }

    /**
     * Equality is based on the underlying cause's type and message (see {@link DbSecretException#equals}).
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getCause());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VaultSecretException exception)) return false;
        if (this.getCause() == null || exception.getCause() == null) return false;
        return Objects.equals(this.getCause().getClass(), exception.getCause().getClass()) && Objects.equals(this.getCause().getMessage(), exception.getCause().getMessage());
    }

    @Override
    public String toString() {
        return """
                VaultSecretException{
                 message: %s,
                 cause: %s,
                 stackTrace: %s
                }""".formatted(getMessage(), getCause(), Arrays.stream(getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
    }
}
