package de.victorarcon.pwdgenerator.control;

import io.github.jopenlibs.vault.response.LogicalResponse;

/**
 * Abstraction over writing a secret to HashiCorp Vault.
 *
 * Kept as an interface (with {@link DefaultVaultClient} as the real implementation) so that
 * {@link UpdateDataInDbAndVault} can be tested without a live Vault server.
 */
public interface VaultClient {

    /**
     * Updates the value of an existing secret in Vault.
     *
     * @param secretName name/path of the secret (typically the username being rotated)
     * @param value the new value to store (the newly generated password)
     * @return a {@link Success} wrapping Vault's response, or a {@link Failure} if the write failed
     */
    Try<LogicalResponse> updatePasswordIfExists(String secretName, String value);

}
