package de.victorarcon.pwdgenerator.control;

import io.github.jopenlibs.vault.response.LogicalResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Handles the coordinated update of user credentials in both Oracle DB and Vault.
 *
 * This class performs transactional updates by:
 * - Executing SQL statements to change user passwords in the database
 * - Writing updated secrets to Vault
 *
 * It uses a {@link Supplier} for lazy initialization of both the database connection and Vault client.
 * All operations are wrapped in {@link Try} to provide functional error handling.
 */
public final class UpdateDataInDbAndVault {

    private static final Log LOG = LogFactory.getLog(UpdateDataInDbAndVault.class);

    private final Supplier<Optional<Connection>> connectionSupplier;

    private final String alterUserTemplate;

    private final Supplier<VaultClient> client;

    /**
     * Constructs an instance for updating user credentials in DB and Vault.
     *
     * @param connectionSupplier supplier of an optional database connection
     * @param client supplier of a {@link VaultClient} instance
     */
    public UpdateDataInDbAndVault(Supplier<Optional<Connection>> connectionSupplier, Supplier<VaultClient> client) {
        this.connectionSupplier = connectionSupplier;
        this.alterUserTemplate = "ALTER USER %s IDENTIFIED BY \"%s\"";
        this.client = client;
    }

    /**
     * Executes password updates for a map of usernames and passwords.
     *
     * For each entry:
     * - Updates the password in Vault
     * - Executes an SQL statement to update the password in Oracle DB
     *
     * If the database connection is unavailable, returns a single {@link Failure}.
     *
     * @param userToPassword map of usernames to new password values
     * @return list of {@link Try} results for each update attempt
     */
    // Map-> Username - Password
    public List<Try<String>> doInTransaction(Map<String, String> userToPassword) {
        try (var connection = connectionSupplier.get().orElseThrow(() -> new IllegalArgumentException("No connection with DB!"))) {
            return userToPassword.entrySet()
                    .stream()
                    .map(userNameToPassword -> doWorkInDbAndVault(connection,
                            userNameToPassword.getKey(),
                            userNameToPassword.getValue(),
                            () -> client.get().updatePasswordIfExists(userNameToPassword.getKey(), userNameToPassword.getValue())))
                    .toList();
        } catch (Exception e) {
            LOG.error(e);

            return List.of(new Failure<>(e));
        }
    }

    /**
     * Performs the update of a single user's password in both Vault and DB.
     *
     * Steps:
     * 1. Attempts to update the password in Vault.
     * 2. If successful, executes an SQL statement to update the password in Oracle DB.
     * 3. Returns a {@link Success} if both operations succeed, or a {@link Failure} otherwise.
     *
     * @param connection active database connection
     * @param username the username to update
     * @param password the new password
     * @param vaultAction supplier of the Vault update operation
     * @return {@link Try} representing the outcome of the combined update
     */
    private Try<String> doWorkInDbAndVault(Connection connection, String username, String password, Supplier<Try<LogicalResponse>> vaultAction) {
        var vaultTry = vaultAction.get();
        if (!vaultTry.isSuccess()) {
            return new Failure<>(vaultTry.getError());
        }

        var sql = alterUserTemplate.formatted(username, password);
        try (var statement = connection.prepareStatement(sql)) {
            var dbTry = Try.of(statement::executeUpdate);

            if (!dbTry.isSuccess()) {
                return new Failure<>(dbTry.getError());
            }
        } catch (SQLException e) {
            LOG.error(e);

            return new Failure<>(DbSecretException.forSecret(username, e));
        }

        return new Success<>(username);
    }
}
