package com.quorum.tessera.key.vault.hashicorp;

import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultVersionedKeyValueTemplate;

public interface VaultVersionedKeyValueTemplateFactory {

  default VaultVersionedKeyValueTemplate createVaultVersionedKeyValueTemplate(
      VaultOperations vaultOperations, String path) {
    return new VaultVersionedKeyValueTemplate(vaultOperations, path);
  }
}
