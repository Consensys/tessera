package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import java.util.Map;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;

class KeyValueOperationsDelegate {

  private final VaultVersionedKeyValueOperations keyValueOperations;

  KeyValueOperationsDelegate(VaultVersionedKeyValueOperations keyValueOperations) {
    this.keyValueOperations = keyValueOperations;
  }

  Versioned<Map<String, Object>> get(HashicorpGetSecretData getSecretData) {
    // if version 0 then latest version retrieved
    return keyValueOperations.get(
        getSecretData.getSecretName(), Versioned.Version.from(getSecretData.getSecretVersion()));
  }

  Versioned.Metadata set(HashicorpSetSecretData setSecretData) {
    return keyValueOperations.put(setSecretData.getSecretName(), setSecretData.getNameValuePairs());
  }
}
