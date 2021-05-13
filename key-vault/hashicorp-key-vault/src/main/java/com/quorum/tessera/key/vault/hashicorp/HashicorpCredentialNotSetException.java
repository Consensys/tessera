package com.quorum.tessera.key.vault.hashicorp;

class HashicorpCredentialNotSetException extends IllegalStateException {

  HashicorpCredentialNotSetException(String message) {
    super(message);
  }
}
