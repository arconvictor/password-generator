package de.victorarcon.pwdgenerator.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.json.JsonObject;
import io.github.jopenlibs.vault.response.LogicalResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link VaultClient} for interacting with HashiCorp Vault.
 *
 * This class handles:
 * - Configuration and initialization of the Vault client
 * - Reading existing secrets
 * - Writing updated secrets
 * - Error handling and retry logic
 *
 * It uses the JOpenLibs Vault SDK and integrates with Jackson for JSON parsing.
 * All operations are wrapped in {@link Try} to provide functional error handling.
 */
public class DefaultVaultClient implements VaultClient {

    private static final Logger LOG = LogManager.getLogger(DefaultVaultClient.class);

    private final Vault client;

    private final String path;

    /**
     * Initializes the Vault client with the given configuration.
     *
     * @param baseUrl        the Vault server URL
     * @param token          the authentication token
     * @param secretPath     the base path to secrets in Vault
     * @param engineVersion  the version of the Vault KV engine (e.g. 2)
     * @throws VaultSecretException if the client fails to initialize
     */
    public DefaultVaultClient(String baseUrl, char[] token, String secretPath, int engineVersion) {
        this.path = secretPath;

        try {
            var config = new VaultConfig()
                    .address(baseUrl)
                    .token(String.valueOf(token))
                    .engineVersion(engineVersion)
                    .build();

            client = Vault.create(config)
                    .withRetries(3, 3000);

        } catch (VaultException e) {
            LOG.error(e);

            throw VaultSecretException.forSecret(secretPath, e);
        }
    }

    /**
     * Updates the password for a given secret if it already exists in Vault.
     *
     * First attempts to read the existing secret. If successful, writes the new password.
     * If the secret does not exist or reading fails, returns a {@link Failure} with wrapped exception.
     *
     * @param secretName the name of the secret to update
     * @param value      the new password value
     * @return a {@link Try} containing the result of the write operation or failure
     */
    @Override
    public Try<LogicalResponse> updatePasswordIfExists(String secretName, String value) {
        var tryPassword = readPassword(secretName);
        if (!tryPassword.isSuccess()) {
            return new Failure<>(VaultSecretException.forSecret(secretName, tryPassword.getError()));
        }

        return writePassword(secretName, value);
    }

    /**
     * Reads the current password value from Vault for the given secret.
     *
     * Parses the nested "data" field from the Vault response and extracts the "password" key.
     * If the secret is missing or malformed, returns a {@link Failure}.
     *
     * @param secretName the name of the secret to read
     * @return a {@link Try} containing the password string or failure
     */
    private Try<String> readPassword(String secretName) {
        return Try.of(() -> {
            var data = client.logical()
                    .read(path + "/" + secretName)
                    .getData()
                    .get("data");

            return (String) new ObjectMapper()
                    .readValue(Optional.ofNullable(data).orElseThrow(() -> new IllegalArgumentException("secret not found")), Map.class)
                    .get("password");
        });
    }

    /**
     * Writes a new password value to Vault for the given secret.
     *
     * Constructs a payload with the "password" field and sends it to Vault.
     * Returns the logical response wrapped in a {@link Try}.
     *
     * @param secretName the name of the secret to write
     * @param value      the new password value
     * @return a {@link Try} containing the Vault response or failure
     */
    private Try<LogicalResponse> writePassword(String secretName, String value) {
        var payload = Map.<String, Object>of("data", new JsonObject().add("password", value));

        return Try.of(() -> client.logical()
                .write(path + "/" + secretName, payload));
    }
}
