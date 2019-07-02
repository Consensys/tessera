package com.quorum.tessera.key.vault.hashicorp;

import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.core.VaultVersionedKeyValueTemplate;

class KeyValueOperationsDelegateFactory {

    private final VaultOperations vaultOperations;

    KeyValueOperationsDelegateFactory(VaultOperations vaultOperations) {
        this.vaultOperations = vaultOperations;
    }

    KeyValueOperationsDelegate create(String secretEngineName) {
        VaultVersionedKeyValueOperations keyValueOperations =
                new VaultVersionedKeyValueTemplate(vaultOperations, secretEngineName);

        return new KeyValueOperationsDelegate(keyValueOperations);
    }
}
