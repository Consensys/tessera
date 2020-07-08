package com.quorum.tessera.key.vault.azure;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;

import java.util.Objects;

public class AzureKeyVaultService implements KeyVaultService<AzureSetSecretData, AzureGetSecretData> {

    private AzureSecretClientDelegate secretClient;

    AzureKeyVaultService(AzureSecretClientDelegate azureSecretClientDelegate) {
        this.secretClient = Objects.requireNonNull(azureSecretClientDelegate);
    }

    @Override
    public String getSecret(AzureGetSecretData azureGetSecretData) {
        KeyVaultSecret secret;

        try {
            secret = secretClient.getSecret(azureGetSecretData.getSecretName(), azureGetSecretData.getSecretVersion());
        } catch (ResourceNotFoundException e) {
            throw new VaultSecretNotFoundException("Azure Key Vault secret " + azureGetSecretData.getSecretName() + " was not found in vault " + secretClient.getVaultUrl());
        }

        return secret.getValue();
    }

    @Override
    public Object setSecret(AzureSetSecretData azureSetSecretData) {
        return secretClient.setSecret(azureSetSecretData.getSecretName(), azureSetSecretData.getSecret());
    }
}
