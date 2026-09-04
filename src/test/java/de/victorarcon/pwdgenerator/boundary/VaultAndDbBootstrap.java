package de.victorarcon.pwdgenerator.boundary;

import de.victorarcon.UrlTestUtils;
import org.junit.ClassRule;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

public class VaultAndDbBootstrap {

    protected static final String VAULT_HOST_ADDRESS;

    protected static final String DB_HOST_ADDRESS;

    @ClassRule
    public static DockerComposeContainer environment = new DockerComposeContainer<>(new File("docker-compose-test.yml"))
            .withExposedService("vault",
                    8200,
                    Wait.forListeningPort())
            .withExposedService("db",
                    1521,
                    Wait.forSuccessfulCommand("./healthcheck.sh"));

    static {
        environment.start();

        var vaultHostAddr = environment.getServiceHost("vault", 8200);
        var vaultHostPort = environment.getServicePort("vault", 8200);
        VAULT_HOST_ADDRESS = UrlTestUtils.resolveHttpUrl(vaultHostAddr, vaultHostPort);

        var dbHostAddr = environment.getServiceHost("db", 1521);
        var dbHostPort = environment.getServicePort("db", 1521);
        DB_HOST_ADDRESS = UrlTestUtils.resolveJdbcUrl(dbHostAddr, dbHostPort, "XE");
    }
}