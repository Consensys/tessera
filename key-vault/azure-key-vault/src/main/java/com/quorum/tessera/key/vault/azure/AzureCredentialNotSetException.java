package com.quorum.tessera.key.vault.azure;

class AzureCredentialNotSetException extends IllegalStateException {

  AzureCredentialNotSetException(String message) {
    super(message);
  }
}
