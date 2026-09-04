package de.victorarcon.pwdgenerator.entity;

import de.victorarcon.pwdgenerator.control.DbSecretException;
import de.victorarcon.pwdgenerator.control.Failure;
import de.victorarcon.pwdgenerator.control.Success;
import de.victorarcon.pwdgenerator.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for querying user existence in an Oracle database.
 *
 * This class checks whether a list of usernames exists in the database by executing
 * a parameterized SQL query against the {@code dba_users} system view.
 *
 * Each result is wrapped in a {@link Try} object to capture success or failure,
 * enabling functional error handling and reporting.
 */
public class UserDataRepository {

    private static final String SELECT_QUERY = "SELECT username FROM dba_users WHERE username=UPPER(?)";

    private static final Logger LOG = LogManager.getLogger(UserDataRepository.class);

    private final DataSource dataSource;

    public UserDataRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Builds a report indicating which usernames exist in the database.
     *
     * For each username:
     * - Executes a query to check existence in {@code dba_users}
     * - Wraps the result in a {@link Success} if found, or {@link Failure} if not
     *
     * If the database connection fails, returns a single {@link Failure} with the exception.
     *
     * @param usernamesListForCheckDB list of usernames to verify
     * @return list of {@link Try} results for each username
     */
    public List<Try<String>> buildUsersReport(List<String> usernamesListForCheckDB) {
        var identifiers = new ArrayList<Try<String>>();

        try (var conn = dataSource.getConnection().orElseThrow(() -> new SQLException("Cannot establish connection with DB"));
             var stmt = conn.prepareStatement(SELECT_QUERY)) {

            for (var identifier : usernamesListForCheckDB) {
                stmt.setString(1, identifier);

                var result = buildUsersReport(identifier, stmt);
                identifiers.add(result);
            }

            return identifiers;
        } catch (SQLException e) {
            LOG.error(e);

            return List.of(new Failure<>(e));
        }
    }

    /**
     * Executes the SQL query to check if a single username exists in the database.
     *
     * @param identifier the username to verify
     * @param stmt the prepared statement with the query
     * @return {@link Success} if the user exists, {@link Failure} otherwise
     */
    private Try<String> buildUsersReport(String identifier, PreparedStatement stmt) {
        try (var resultSet = stmt.executeQuery()) {
            return resultSet.next() ? new Success<>(identifier) : new Failure<>(DbSecretException.forSecret(identifier, new SQLException(identifier + " could not be found in the DB")));
        } catch (SQLException e) {
            LOG.error(e);

            return new Failure<>(DbSecretException.forSecret(identifier, e));
        }
    }
}
