package com.quorum.tessera.key.vault.azure;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AzureKeyVaultService implements KeyVaultService<AzureSetSecretData, AzureGetSecretData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureKeyVaultService.class);

    private AzureSecretClientDelegate secretClient;

    AzureKeyVaultService(AzureSecretClientDelegate azureSecretClientDelegate) {
        this.secretClient = Objects.requireNonNull(azureSecretClientDelegate);
    }

    @Override
    public String getSecret(AzureGetSecretData azureGetSecretData) {
        LOGGER.info(
                "CHRISSY getSecret {} {}", azureGetSecretData.getSecretName(), azureGetSecretData.getSecretVersion());
        LOGGER.info("CHRISSY getSecret 1");
        KeyVaultSecret secret;

        try {
            LOGGER.info("CHRISSY getSecret 2");
            secret = secretClient.getSecret(azureGetSecretData.getSecretName(), azureGetSecretData.getSecretVersion());
        } catch (ResourceNotFoundException e) {
            LOGGER.info("CHRISSY getSecret 3");
            throw new VaultSecretNotFoundException(
                    "Azure Key Vault secret "
                            + azureGetSecretData.getSecretName()
                            + " was not found in vault "
                            + secretClient.getVaultUrl());
        }
        LOGGER.info("CHRISSY getSecret 4");

        return secret.getValue();
    }

    @Override
    public Object setSecret(AzureSetSecretData azureSetSecretData) {
        return secretClient.setSecret(azureSetSecretData.getSecretName(), azureSetSecretData.getSecret());
    }
}
