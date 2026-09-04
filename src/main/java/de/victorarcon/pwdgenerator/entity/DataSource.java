package de.victorarcon.pwdgenerator.entity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Abstraction over obtaining a database connection.
 *
 * Exists so that {@link UserDataRepository} and the DB update logic can depend on
 * a connection source without being coupled to a concrete implementation
 * (e.g. {@link OracleDataSource}), which also makes them straightforward to test.
 */
public interface DataSource {

    /**
     * Obtains a connection to the database.
     *
     * @return an open {@link Connection}, or {@link Optional#empty()} if it could not be established
     * @throws SQLException if the underlying driver reports a fatal error while connecting
     */
    Optional<Connection> getConnection() throws SQLException;
}
