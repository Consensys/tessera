package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;

public class HashicorpKeyVaultService implements KeyVaultService {

    //TODO Make use of methods from KeyVaultService

    private String vaultUrl;
    private VaultTemplate vaultTemplate;

    public HashicorpKeyVaultService(HashicorpKeyVaultConfig keyVaultConfig, VaultTemplate vaultTemplate) {
        this.vaultUrl = keyVaultConfig.getUrl();
        this.vaultTemplate = vaultTemplate;
    }

    public String getHashicorpSecret(String secretPath, String secretName) {
        VaultResponse response = vaultTemplate.read(secretPath);

        if(response == null) {
            throw new VaultSecretNotFoundException("Hashicorp Vault secret not found at path " + secretPath + " in vault " + vaultUrl);
        }

        if(response.getData() == null) {
            throw new VaultSecretNotFoundException("Value for secret key " + secretName + " not found at path " + secretPath + " in vault " + vaultUrl);
        }

        return response.getData().get(secretName).toString();
    }

    public void setHashicorpSecret(String secretPath, Map<String, String> keyValuePairs) {
        vaultTemplate.write(secretPath, keyValuePairs);
    }

    @Override
    public String getSecret(String secretName) {
        return null;
    }

    @Override
    public Object setSecret(String secretName, String secret) {
        return null;
    }
}
