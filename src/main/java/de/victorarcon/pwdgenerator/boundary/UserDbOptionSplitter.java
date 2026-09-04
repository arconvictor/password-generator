package de.victorarcon.pwdgenerator.boundary;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Parses and validates a database connection string in the format: {@code user@dbname:port:sid}.
 *
 * This utility class extracts individual components such as:
 * - Username
 * - Database name
 * - Port number
 * - SID (System Identifier)
 *
 * It also provides a lowercase version of the SID for use as a schema name.
 *
 * Example input: {@code admin@oracledb:1521:DEV}
 */
final class UserDbOptionSplitter {

    private static final Pattern PATTERN = Pattern.compile("^\\w+@\\S+:\\d+:\\w+$");

    private final String identifier;

    private final String databaseName;

    private final Long portNumber;
    
    private final String sid;

    /**
     * Constructs a {@code UserDbOptionSplitter} by parsing the given connection string.
     *
     * @param userDb the connection string in format {@code user@dbname:port:sid}
     * @throws NullPointerException if {@code userDb} is null
     * @throws IllegalArgumentException if the input does not match the expected format
     */
    public UserDbOptionSplitter(String userDb) {
        Objects.requireNonNull(userDb, "userDb must not be null");

        var matches = PATTERN.matcher(userDb);
        if (!matches.find()) {
            throw new IllegalArgumentException("Invalid input string " + userDb);
        }

        // user@dbname:portnumber:sid
        var parts = userDb.split("@");
        identifier = parts[0];
        var dbNamePortSid = parts[1].split(":");

        databaseName = dbNamePortSid[0];
        portNumber = Long.parseLong(dbNamePortSid[1]);
        sid = dbNamePortSid[2];
    }

    Long getPortNumber() {
        return portNumber;
    }

    String getDatabaseName() {
        return databaseName;
    }

    String getUsername() {
        return identifier;
    }

    String getSid() {
        return sid;
    }

    String getSchema() {
        return sid.toLowerCase();
    }
}
