package de.victorarcon.pwdgenerator.control;

import de.victorarcon.pwdgenerator.TestDataHelper;
import de.victorarcon.pwdgenerator.VaultClientTestHelper;
import de.victorarcon.pwdgenerator.boundary.VaultAndDbBootstrap;
import de.victorarcon.pwdgenerator.entity.OracleDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

final class UpdateDataInDbAndVaultTest extends VaultAndDbBootstrap {

    private static final String VAULT_TOKEN = "VAULT_TOKEN";

    private static final String VAULT_SECRET_PATH = "secret/data/hmot";

    private VaultClientTestHelper vaultClientHelper;

    private TestDataHelper testDataHelper;

    private UpdateDataInDbAndVault testSubject;

    @BeforeEach
    void setUp() {
        vaultClientHelper = new VaultClientTestHelper(VAULT_HOST_ADDRESS, VAULT_TOKEN.toCharArray(), VAULT_SECRET_PATH, 2);

        var dataSource = new OracleDataSource(DB_HOST_ADDRESS, "testUser", "password".toCharArray());
        testDataHelper = new TestDataHelper(dataSource);

        var vaultClient = new DefaultVaultClient(VAULT_HOST_ADDRESS, VAULT_TOKEN.toCharArray(), VAULT_SECRET_PATH, 2);
        testSubject = new UpdateDataInDbAndVault(dataSource::getConnection, () -> vaultClient);
    }

    @Nested
    class ChangeData {

        private final Map<String, String> initData = Map.of(
                "dummy1", "password",
                "dummy2", "password"
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
        void can_change_single_user_password() {
            // Act
            var report = testSubject.doInTransaction(Map.of("dummy1", "password_!"));

            // Assert
            var vaultPasswordAfterUpdate = vaultClientHelper.readPassword("dummy1");

            assertThat(report).containsExactlyInAnyOrder(new Success<>("dummy1"));
            assertThat(vaultPasswordAfterUpdate.getResult()).isEqualTo("password_!");
        }

        @Test
        void can_change_two_users_passwords() {
            // Arrange
            var expectedDummy1Pwd = "password_!";
            var expectedDummy2Pwd = "password_@9";

            // Act
            var report = testSubject.doInTransaction(Map.of(
                    "dummy1", expectedDummy1Pwd,
                    "dummy2", expectedDummy2Pwd
            ));

            // Assert
            var vaultPasswordAfterUpdateDummy1 = vaultClientHelper.readPassword("dummy1");
            var vaultPasswordAfterUpdateDummy2 = vaultClientHelper.readPassword("dummy2");

            assertThat(report).containsExactlyInAnyOrder(new Success<>("dummy1"), new Success<>("dummy2"));

            assertThat(vaultPasswordAfterUpdateDummy1.getResult()).isEqualTo("password_!");
            assertThat(vaultPasswordAfterUpdateDummy2.getResult()).isEqualTo("password_@9");
        }

        @Test
        void can_change_two_users_passwords_if_one_fails() {
            // Arrange
            var expectedDummy1Pwd = "password_!";
            var expectedDummy2Pwd = "password_@9";

            // Act
            var report = testSubject.doInTransaction(Map.of(
                    "dummy1", expectedDummy1Pwd,
                    "not_existing", "password_xyz",
                    "dummy2", expectedDummy2Pwd
            ));

            // Assert
            var vaultPasswordAfterUpdate = vaultClientHelper.readPassword("dummy1");
            var vaultPasswordAfterUpdate2 = vaultClientHelper.readPassword("dummy2");

            assertThat(report).containsExactlyInAnyOrder(
                    new Success<>("dummy1"),
                    new Failure<>(VaultSecretException.forSecret("not_existing", new IllegalArgumentException("secret not found"))),
                    new Success<>("dummy2"));

            assertThat(vaultPasswordAfterUpdate.getResult()).isEqualTo(expectedDummy1Pwd);
            assertThat(vaultPasswordAfterUpdate2.getResult()).isEqualTo(expectedDummy2Pwd);
        }

        @Test
        void wont_change_any_password() {
            // Act
            var report = testSubject.doInTransaction(Map.of(
                    "not_existing_1", "password_xyz",
                    "not_existing_2", "password_xyz"
            ));

            // Assert
            assertThat(report).containsExactlyInAnyOrder(
                    new Failure<>(VaultSecretException.forSecret("not_existing_1", new IllegalArgumentException("secret not found"))),
                    new Failure<>(VaultSecretException.forSecret("not_existing_2", new IllegalArgumentException("secret not found"))));
        }
    }
}
