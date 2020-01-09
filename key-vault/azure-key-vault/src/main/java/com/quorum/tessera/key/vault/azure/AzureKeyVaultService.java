package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;

import java.util.Objects;

public class AzureKeyVaultService implements KeyVaultService<AzureSetSecretData, AzureGetSecretData> {

    private String vaultUrl;

    private AzureKeyVaultClientDelegate azureKeyVaultClientDelegate;

    AzureKeyVaultService(KeyVaultConfig keyVaultConfig, AzureKeyVaultClientDelegate azureKeyVaultClientDelegate) {
        this(keyVaultConfig.getProperty("url").get(), azureKeyVaultClientDelegate);
    }

    AzureKeyVaultService(String vaultUrl, AzureKeyVaultClientDelegate azureKeyVaultClientDelegate) {
        this.vaultUrl = Objects.requireNonNull(vaultUrl);
        this.azureKeyVaultClientDelegate = Objects.requireNonNull(azureKeyVaultClientDelegate);
    }

    @Override
    public String getSecret(AzureGetSecretData azureGetSecretData) {
        SecretBundle secretBundle;

        if (azureGetSecretData.getSecretVersion() != null) {
            secretBundle =
                    azureKeyVaultClientDelegate.getSecret(
                            vaultUrl, azureGetSecretData.getSecretName(), azureGetSecretData.getSecretVersion());
        } else {
            secretBundle = azureKeyVaultClientDelegate.getSecret(vaultUrl, azureGetSecretData.getSecretName());
        }

        if (secretBundle == null) {
            throw new VaultSecretNotFoundException(
                    "Azure Key Vault secret "
                            + azureGetSecretData.getSecretName()
                            + " was not found in vault "
                            + vaultUrl);
        }

        return secretBundle.value();
    }

    @Override
    public Object setSecret(AzureSetSecretData azureSetSecretData) {
        SetSecretRequest setSecretRequest =
                new SetSecretRequest.Builder(
                                vaultUrl, azureSetSecretData.getSecretName(), azureSetSecretData.getSecret())
                        .build();

        return this.azureKeyVaultClientDelegate.setSecret(setSecretRequest);
    }
}
