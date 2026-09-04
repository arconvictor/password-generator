package de.victorarcon.pwdgenerator;

import de.victorarcon.pwdgenerator.control.Failure;
import de.victorarcon.pwdgenerator.control.Success;
import de.victorarcon.pwdgenerator.control.Try;
import de.victorarcon.pwdgenerator.control.VaultSecretException;
import de.victorarcon.pwdgenerator.entity.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public final class TestDataHelper {

    private static final Logger LOG = LogManager.getLogger(TestDataHelper.class);

    private final DataSource dataSource;

    public TestDataHelper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Try<String> changePasswordByIdentifier(String identifier, String password) {
        var update = "ALTER USER %s IDENTIFIED BY \"%s\"".formatted(identifier, password);

        try (var connection = dataSource.getConnection().orElseThrow(() -> new IllegalArgumentException("Cannot establish connection with DB!"))) {
            var stmt = connection.prepareStatement(update);

            stmt.executeUpdate();

            return new Success<>(identifier);
        } catch (SQLException e) {
            LOG.error(e);

            return new Failure<>(VaultSecretException.forSecret(identifier, e));
        }
    }

}


