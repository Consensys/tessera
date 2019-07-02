package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;

public interface KeyVaultService {
    String getSecret(GetSecretData getSecretData);

    Object setSecret(SetSecretData setSecretData);
}
