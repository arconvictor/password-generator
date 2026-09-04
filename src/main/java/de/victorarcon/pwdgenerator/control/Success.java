package de.victorarcon.pwdgenerator.control;

/**
 * Represents a successful result of an operation wrapped in a {@link Try}.
 *
 * This record holds the result value and overrides behavior to indicate success.
 * Calling {@link #getError()} on this type will throw an exception, as no error is present.
 *
 * @param <T> the type of the successful result
 */
public record Success<T>(T data) implements Try<T> {

    /**
     * Returns the result value of the successful operation.
     *
     * @return the result value
     */
    @Override
    public T getResult() {
        return data;
    }

    /**
     * Throws an exception because this instance represents a successful result.
     *
     * @throws RuntimeException always, indicating invalid access to error on a success
     */
    @Override
    public Throwable getError() {
        throw new RuntimeException("Invalid invocation");
    }

}