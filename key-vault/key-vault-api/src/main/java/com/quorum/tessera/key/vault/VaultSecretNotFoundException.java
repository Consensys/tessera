package com.quorum.tessera.key.vault;

public class VaultSecretNotFoundException extends RuntimeException {

  public VaultSecretNotFoundException(String message) {
    super(message);
  }
}
