package de.victorarcon.pwdgenerator.control;

import java.util.concurrent.Callable;

/**
 * Represents the result of an operation that may succeed or fail.
 *
 * This sealed interface abstracts over two possible outcomes:
 * - {@link Success}: wraps a successful result
 * - {@link Failure}: wraps an exception or error
 *
 * It provides a unified way to handle operations that may throw exceptions,
 * without using traditional try-catch blocks directly.
 *
 * @param <T> the type of the successful result
 */
public sealed interface Try<T> permits Success, Failure {

    /**
     * Returns the result of the operation if successful.
     *
     * @return the result value
     * @throws UnsupportedOperationException if called on a {@link Failure}
     */
    T getResult();

    /**
     * Returns the error or exception that occurred during the operation.
     *
     * @return the {@link Throwable} cause of failure
     * @throws UnsupportedOperationException if called on a {@link Success}
     */
    Throwable getError();

    /**
     * Indicates whether the operation was successful.
     *
     * @return {@code true} for {@link Success}, {@code false} for {@link Failure}
     */
    default boolean isSuccess() {
        return true;
    }

    /**
     * Executes a {@link Callable} and wraps its result in a {@link Try}.
     *
     * If the callable completes successfully, returns a {@link Success}.
     * If it throws an exception, returns a {@link Failure} with the cause.
     *
     * @param action the operation to execute
     * @param <T> the type of the result
     * @return a {@link Try} representing success or failure
     */
    static <T> Try<T> of(Callable<T> action) {
        try {
            var result = action.call();

            return new Success<>(result);
        } catch (Throwable throwable) {
            return new Failure<>(throwable);
        }
    }
}