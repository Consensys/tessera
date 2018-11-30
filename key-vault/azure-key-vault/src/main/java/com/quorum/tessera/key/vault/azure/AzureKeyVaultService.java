package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.quorum.tessera.config.AzureKeyVaultConfig;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;

import java.util.Map;
import java.util.Objects;

public class AzureKeyVaultService implements KeyVaultService {
    private String vaultUrl;
    private AzureKeyVaultClientDelegate azureKeyVaultClientDelegate;

    public AzureKeyVaultService(AzureKeyVaultConfig keyVaultConfig, AzureKeyVaultClientDelegate azureKeyVaultClientDelegate) {
        if(Objects.nonNull(keyVaultConfig)) {
            this.vaultUrl = keyVaultConfig.getUrl();
        }

        this.azureKeyVaultClientDelegate = azureKeyVaultClientDelegate;
    }

    public String getSecret(String secretName) {
        SecretBundle secretBundle = azureKeyVaultClientDelegate.getSecret(vaultUrl, secretName);

        if(secretBundle == null) {
            throw new VaultSecretNotFoundException("Azure Key Vault secret " + secretName + " was not found in vault " + vaultUrl);
        }

        return secretBundle.value();
    }

    @Override
    public String getSecretFromPath(String secretPath, String secretName) {
        return null;
    }

    @Override
    public SecretBundle setSecret(String secretName, String secret) {
        SetSecretRequest setSecretRequest = new SetSecretRequest.Builder(vaultUrl, secretName, secret).build();

        return this.azureKeyVaultClientDelegate.setSecret(setSecretRequest);
    }

    @Override
    public Object setSecretAtPath(String secretPath, Map<String, Object> secretData) {
        return null;
    }
}
