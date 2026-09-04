package de.victorarcon.pwdgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.victorarcon.pwdgenerator.control.Try;
import de.victorarcon.pwdgenerator.control.VaultSecretException;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.json.JsonObject;
import io.github.jopenlibs.vault.response.LogicalResponse;

import java.util.Map;
import java.util.Optional;

public class VaultClientTestHelper {

    private final Vault client;

    private final String path;

    public VaultClientTestHelper(String baseUrl, char[] token, String secretPath, int engineVersion) {
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
            throw VaultSecretException.forSecret(secretPath, e);
        }
    }

    public Try<String> readPassword(String secretName) {
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

    public Try<LogicalResponse> writePassword(String secretName, String value) {
        var payload = Map.<String, Object>of("data", new JsonObject().add("password", value));

        return Try.of(() -> client.logical()
                .write(path + "/" + secretName, payload));
    }

}
