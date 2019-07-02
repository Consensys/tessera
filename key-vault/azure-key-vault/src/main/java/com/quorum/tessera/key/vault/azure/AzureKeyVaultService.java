package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.quorum.tessera.config.AzureKeyVaultConfig;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;
import com.quorum.tessera.key.vault.KeyVaultException;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;

import java.util.Objects;

public class AzureKeyVaultService implements KeyVaultService {
    private String vaultUrl;
    private AzureKeyVaultClientDelegate azureKeyVaultClientDelegate;

    AzureKeyVaultService(AzureKeyVaultConfig keyVaultConfig, AzureKeyVaultClientDelegate azureKeyVaultClientDelegate) {
        Objects.requireNonNull(keyVaultConfig);
        Objects.requireNonNull(azureKeyVaultClientDelegate);

        this.vaultUrl = keyVaultConfig.getUrl();
        this.azureKeyVaultClientDelegate = azureKeyVaultClientDelegate;
    }

    @Override
    public String getSecret(GetSecretData getSecretData) {
        if (!(getSecretData instanceof AzureGetSecretData)) {
            throw new KeyVaultException(
                    "Incorrect data type passed to AzureKeyVaultService.  Type was " + getSecretData.getType());
        }

        AzureGetSecretData azureGetSecretData = (AzureGetSecretData) getSecretData;

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
    public Object setSecret(SetSecretData setSecretData) {
        if (!(setSecretData instanceof AzureSetSecretData)) {
            throw new KeyVaultException(
                    "Incorrect data type passed to AzureKeyVaultService.  Type was " + setSecretData.getType());
        }

        AzureSetSecretData azureSetSecretData = (AzureSetSecretData) setSecretData;

        SetSecretRequest setSecretRequest =
                new SetSecretRequest.Builder(
                                vaultUrl, azureSetSecretData.getSecretName(), azureSetSecretData.getSecret())
                        .build();

        return this.azureKeyVaultClientDelegate.setSecret(setSecretRequest);
    }
}
