package de.victorarcon.pwdgenerator.boundary;

import de.victorarcon.pwdgenerator.control.DefaultVaultClient;
import de.victorarcon.pwdgenerator.control.VaultClient;
import de.victorarcon.pwdgenerator.entity.OracleDataSource;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static picocli.CommandLine.Option;

/**
 * Picocli command definition for the CLI: declares every {@code -db}/{@code -u}/{@code -f}/etc.
 * option, and wires the parsed values into the actual update flow in {@link #call()}.
 */
@Command(name = "Menu", description = "pwd-generator")
public class CmdPasswordGen implements Callable<Integer> {

    @Option(names = {"-db", "--database"},
            required = true,
            description = "Enter your identifier, the name of the database and the port number of the database service example: user@dbname:portnumber:sid")
    private String userDb;

    @Option(names = {"-u", "--usernames"}, description = "Enter the usernames", split = ",")
    private List<String> usernames;

    @Option(names = {"-f", "--file"}, description = "Enter the address of the file to be read")
    private Path path;

    @Option(names = {"-vault", "--vault-url"},
            defaultValue = "https://vault.example.com",
            description = "The url of the vault server")
    private String vaultUrl;

    @Option(names = {"-s", "--secret-prefix"},
            defaultValue = "db/data/",
            description = "The prefix to the secret path")
    private String secretPrefix;

    @Option(names = {"-p", "--password"},
            arity = "0..1",
            required = true,
            description = "The user password", interactive = true,
            hidden = true)
    private char[] password;

    @Option(names = {"-t", "--token"},
            arity = "0..1",
            required = true,
            description = "The user token",
            interactive = true,
            hidden = true)
    private char[] token;

    /**
     * Entry point for the CLI command.
     *
     * This method orchestrates the password update process:
     * - Parses the database connection string.
     * - Builds the Oracle data source with credentials.
     * - Initializes a Vault client for secret management.
     * - Reads user identifiers from CLI or file.
     * - Updates passwords in Vault and logs statistics.
     *
     * @return 0 if execution completes successfully
     */
    @Override
    public Integer call() {
        var userDbSplitter = new UserDbOptionSplitter(userDb);

        var dataSource = new OracleDataSource(getJdbcUrl(userDbSplitter),
                userDbSplitter.getUsername(),
                password);

        Supplier<VaultClient> vaultClient = () -> new DefaultVaultClient(vaultUrl,
                token,
                secretPrefix + userDbSplitter.getSchema(),
                1);

        var identifiers = new IdentifiersReader(usernames, path).read();

        var report = new PasswordUpdater(dataSource, vaultClient).update(identifiers);

        new StatisticsLogger(report, identifiers).log();

        return 0;
    }

    /**
     * Constructs a JDBC URL for Oracle based on the parsed database connection string.
     *
     * Example output: jdbc:oracle:thin:@hostname:1521:SID
     *
     * @param userDbOptionSplitter parsed components of the database string
     * @return a valid JDBC URL for Oracle
     */
    private static String getJdbcUrl(UserDbOptionSplitter userDbOptionSplitter) {
        var databaseName = userDbOptionSplitter.getDatabaseName();
        var portNumber = userDbOptionSplitter.getPortNumber();
        var sid = userDbOptionSplitter.getSid();

        return "jdbc:oracle:thin:@" + databaseName.toLowerCase() + ":" + portNumber + ":" + sid;
    }
}
