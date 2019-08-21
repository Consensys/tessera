package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;

public interface KeyVaultService<T extends SetSecretData, U extends GetSecretData> {

    String getSecret(U getSecretData);

    Object setSecret(T setSecretData);
}
