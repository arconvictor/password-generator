package de.victorarcon.pwdgenerator.boundary;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.vault.VaultContainer;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

class CmdPasswordGenTest {

    private static final String DB_USER = "testUser";

    private static final String DB_PWD = "password";

    private static final String VAULT_TOKEN = "VAULT_TOKEN";

    private static final String VAULT_SECRET_PATH = "secret/data/hmot";

    private static VaultContainer<?> vault;

    @BeforeAll
    static void startContainers() {
        try (var vaultContainer = new VaultContainer<>("hashicorp/vault:1.16.1")) {
            vault = vaultContainer.withVaultToken(VAULT_TOKEN);
        }

        vault.start();
    }

    @AfterAll
    static void afterAll() {
        vault.stop();
    }

    @Test
    void can_call_command_line_tool() {
        String[] args = new String[]{"-db", DB_USER + "@localhost:1521:XE",
                "-u", "dummy1, dummy2, not_existing",
                "-vault", vault.getHttpHostAddress(),
                "-s", VAULT_SECRET_PATH,
                "-p", DB_PWD,
                "-t", VAULT_TOKEN};

        int exitCode = new CommandLine(new CmdPasswordGen()).execute(args);

        assertThat(exitCode).isZero();
    }
}