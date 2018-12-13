package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.core.VaultVersionedKeyValueTemplate;
import org.springframework.vault.support.Versioned;

import java.util.Map;

class HashicorpKeyVaultServiceDelegate {

    Versioned<Map<String, Object>> get(VaultOperations vaultOperations, HashicorpGetSecretData getSecretData) {
        VaultVersionedKeyValueOperations keyValueOperations = new VaultVersionedKeyValueTemplate(vaultOperations, getSecretData.getSecretEngineName());

        return keyValueOperations.get(getSecretData.getSecretName());
    }

    Versioned.Metadata set(VaultOperations vaultOperations, HashicorpSetSecretData setSecretData) {
        VaultVersionedKeyValueOperations keyValueOperations = new VaultVersionedKeyValueTemplate(vaultOperations, setSecretData.getSecretEngineName());

        return keyValueOperations.put(setSecretData.getSecretName(), setSecretData.getNameValuePairs());
    }

}
