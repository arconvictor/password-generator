package de.victorarcon.pwdgenerator.boundary;

import de.victorarcon.pwdgenerator.control.PwdGenerator;
import de.victorarcon.pwdgenerator.control.PwdValidator;
import de.victorarcon.pwdgenerator.control.Try;
import de.victorarcon.pwdgenerator.control.UpdateDataInDbAndVault;
import de.victorarcon.pwdgenerator.control.VaultClient;
import de.victorarcon.pwdgenerator.entity.OracleDataSource;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Orchestrates a full password-rotation run: checks which requested users actually exist
 * in the database, generates new passwords for them, and updates both Vault and the
 * database via {@link UpdateDataInDbAndVault}.
 */
public class PasswordUpdater {

    private final OracleDataSource dataSource;

    private final Supplier<VaultClient> vaultClientCallable;

    public PasswordUpdater(OracleDataSource dataSource, Supplier<VaultClient> vaultClientCallable) {
        this.dataSource = dataSource;
        this.vaultClientCallable = vaultClientCallable;
    }

    /**
     * Updates passwords for the provided list of user identifiers.
     *
     * Execution steps:
     * 1. Builds a {@link UserReport} to separate existing and non-existing users.
     * 2. Generates new passwords for existing users using {@link PwdGenerator} and {@link PwdValidator}.
     * 3. Updates credentials in Oracle DB and Vault using {@link UpdateDataInDbAndVault}.
     * 4. Combines results from both existing and non-existing users into a unified report.
     *
     * @param identifiers list of user identifiers to process
     * @return a list of {@link Try} objects representing success or failure for each user
     */
    List<Try<String>> update(List<String> identifiers) {
        var report = new UserReport(dataSource, identifiers);

        var generator = new PwdGenerator(new PwdValidator());
        var existingIds = report.getExistingUsers()
                .stream()
                .map(Try::getResult)
                .toList();
        var idsAndPwds = generator.generateIdPassMap(existingIds);

        var pwdChange = new UpdateDataInDbAndVault(dataSource::getConnection, vaultClientCallable);

        var nonExistingUsers = report.getNonExistingUsers();
        var updateReport = pwdChange.doInTransaction(idsAndPwds);

        return Stream.of(nonExistingUsers, updateReport)
                .flatMap(Collection::stream)
                .toList();
    }
}
