package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;
import com.quorum.tessera.key.vault.KeyVaultException;
import com.quorum.tessera.key.vault.KeyVaultService;

import java.util.Optional;

public class HashicorpKeyVaultService implements KeyVaultService {

    private final Vault vault;

    public HashicorpKeyVaultService(Vault vault) {
        this.vault = vault;
    }

    @Override
    public String getSecret(GetSecretData getSecretData) {
        if(!(getSecretData instanceof HashicorpGetSecretData)) {
            throw new KeyVaultException("Incorrect data type passed to HashicorpKeyVaultService.  Type was " + getSecretData.getType());
        }

        HashicorpGetSecretData hashicorpGetSecretData = (HashicorpGetSecretData) getSecretData;

        LogicalResponse response;
        try {
            response = vault.logical().read(hashicorpGetSecretData.getSecretPath());
        } catch(VaultException e) {
            throw new HashicorpVaultException("Error getting secret " + hashicorpGetSecretData.getSecretName() + " from path " + hashicorpGetSecretData.getSecretPath() + " - " + e.getMessage());
        }

        return Optional.of(response)
            .map(LogicalResponse::getData)
            .map(data -> data.get(hashicorpGetSecretData.getSecretName()))
            .orElseThrow(() -> new HashicorpVaultException("No secret " + hashicorpGetSecretData.getSecretName() + " found at path " + hashicorpGetSecretData.getSecretPath()));
    }

    @Override
    public Object setSecret(SetSecretData setSecretData) {
        if(!(setSecretData instanceof HashicorpSetSecretData)) {
            throw new KeyVaultException("Incorrect data type passed to HashicorpKeyVaultService.  Type was " + setSecretData.getType());
        }

        HashicorpSetSecretData hashicorpSetSecretData = (HashicorpSetSecretData) setSecretData;

        try {
            return vault.logical().write(hashicorpSetSecretData.getSecretPath(), hashicorpSetSecretData.getNameValuePairs());
        } catch(VaultException e) {
            throw new HashicorpVaultException("Error writing secret to path " + hashicorpSetSecretData.getSecretPath() + " - " + e.getMessage());
        }
    }
}
