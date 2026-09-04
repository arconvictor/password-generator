package de.victorarcon.pwdgenerator.boundary;

import de.victorarcon.pwdgenerator.TestDataHelper;
import de.victorarcon.pwdgenerator.VaultClientTestHelper;
import de.victorarcon.pwdgenerator.control.DbSecretException;
import de.victorarcon.pwdgenerator.control.DefaultVaultClient;
import de.victorarcon.pwdgenerator.control.Failure;
import de.victorarcon.pwdgenerator.control.Success;
import de.victorarcon.pwdgenerator.entity.OracleDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class PasswordUpdaterTest extends VaultAndDbBootstrap {

    private static final String VAULT_TOKEN = "VAULT_TOKEN";

    private static final String VAULT_SECRET_PATH = "secret/data/hmot";

    private TestDataHelper testDataHelper;

    private PasswordUpdater testSubject;

    private VaultClientTestHelper vaultClientHelper;

    @BeforeEach
    void setUp() {
        vaultClientHelper = new VaultClientTestHelper(VAULT_HOST_ADDRESS, VAULT_TOKEN.toCharArray(), VAULT_SECRET_PATH, 2);

        var vaultClient = new DefaultVaultClient(VAULT_HOST_ADDRESS, VAULT_TOKEN.toCharArray(), VAULT_SECRET_PATH, 2);
        var dataSource = new OracleDataSource(DB_HOST_ADDRESS,
                "testUser",
                "password".toCharArray());

        testSubject = new PasswordUpdater(dataSource, () -> vaultClient);
        testDataHelper = new TestDataHelper(dataSource);
    }

    @Nested
    class ChangeData {

        private final Map<String, String> initData = Map.of(
                "dummy1", "password",
                "dummy2", "password",
                "dummy3", "password"
        );

        @BeforeEach
        void initDbAndVault() {
            initData.forEach((secretName, value) -> {
                testDataHelper.changePasswordByIdentifier(secretName, value);
                vaultClientHelper.writePassword(secretName, value);
            });

            initData.forEach((secretName, value) -> assertThat(vaultClientHelper.readPassword(secretName).getResult()).isEqualTo(value));
        }

        @Test
        void change_data_of_existing_users_only() {
            // Arrange
            var data = List.of("dummy1", "dummy2", "dummy3", "not_existing_1", "not_existing_2");

            // Act
            var report = testSubject.update(data);

            assertThat(report).containsExactlyInAnyOrder(
                    new Success<>("dummy1"),
                    new Success<>("dummy2"),
                    new Success<>("dummy3"),
                    new Failure<>(DbSecretException.forSecret("not_existing_1", new SQLException("not_existing_1 could not be found in the DB"))),
                    new Failure<>(DbSecretException.forSecret("not_existing_2", new SQLException("not_existing_2 could not be found in the DB")))
            );
        }
    }

}