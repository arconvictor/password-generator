package de.victorarcon.pwdgenerator.control;

import java.util.Objects;

/**
 * Represents a failed result of an operation wrapped in a {@link Try}.
 *
 * This record holds the {@link Throwable} that caused the failure and overrides behavior to indicate failure.
 * It provides equality and hashing based on the underlying exception.
 *
 * @param <T> the expected result type (unused in failure case)
 */
public record Failure<T>(Throwable throwable) implements Try<T> {

    /**
     * Throws an exception because this instance represents a failed operation.
     *
     * @return never returns normally
     * @throws RuntimeException always, indicating invalid access to result on a failure
     */
    @Override
    public T getResult() {
        throw new RuntimeException("Invalid invocation");
    }

    /**
     * Returns the underlying {@link Throwable} that caused the failure.
     *
     * @return the exception or error that occurred
     */
    @Override
    public Throwable getError() {
        return throwable;
    }

    /**
     * Indicates that this instance represents a failed operation.
     *
     * @return {@code false}
     */
    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Failure<?> failure)) return false;
        return Objects.equals(throwable, failure.throwable);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(throwable);
    }
}