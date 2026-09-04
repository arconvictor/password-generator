package de.victorarcon.pwdgenerator.entity;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * {@link DataSource} implementation backed by an Oracle database, using Commons DBCP2's
 * {@link BasicDataSource} for connection pooling.
 */
public class OracleDataSource implements DataSource {

    private static final Logger LOG = LogManager.getLogger(OracleDataSource.class);

    private final BasicDataSource dataSource;

    /**
     * Configures a pooled data source for an Oracle database.
     *
     * @param jdbc the JDBC URL, e.g. {@code jdbc:oracle:thin:@host:1521:SID}
     * @param user the database username to connect as
     * @param password the database password
     */
    public OracleDataSource(String jdbc, String user, char[] password) {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setUrl(jdbc);
        basicDataSource.setUsername(user);
        basicDataSource.setPassword(String.valueOf(password));

        dataSource = basicDataSource;
    }

    /**
     * Obtains a pooled connection to the Oracle database.
     *
     * @return an open {@link Connection}, or {@link Optional#empty()} if the connection attempt failed
     *         (the underlying {@link SQLException}, if any, is logged rather than propagated)
     */
    @Override
    public Optional<Connection> getConnection() {
        try {
            return Optional.ofNullable(dataSource.getConnection());
        } catch (SQLException e) {
            LOG.error(e);

            return Optional.empty();
        }
    }
}

