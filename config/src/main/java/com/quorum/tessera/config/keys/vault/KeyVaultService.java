package com.quorum.tessera.config.keys.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.quorum.tessera.config.KeyConfiguration;

import java.util.Objects;

public class KeyVaultService {
    private String vaultUrl;

    public KeyVaultService(KeyConfiguration keyConfig) {
        if(Objects.nonNull(keyConfig.getKeyVaultConfig())) {
            this.vaultUrl = keyConfig.getKeyVaultConfig().getUrl();
        }
    }

    public String getSecret(String secretName) {
        KeyVaultClient vaultClient = KeyVaultAuthenticator.getAuthenticatedClient();

        SecretBundle secretBundle = vaultClient.getSecret(vaultUrl, secretName);

        return secretBundle.value();
    }

    public static KeyVaultService create(KeyConfiguration keyConfig) {
        return new KeyVaultService(keyConfig);
    }
}
