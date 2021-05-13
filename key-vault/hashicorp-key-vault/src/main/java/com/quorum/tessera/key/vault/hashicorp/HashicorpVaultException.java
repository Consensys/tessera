package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.key.vault.KeyVaultException;

class HashicorpVaultException extends KeyVaultException {

  HashicorpVaultException(Throwable cause) {
    super(cause);
  }

  HashicorpVaultException(String message) {
    super(message);
  }
}
