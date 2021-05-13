package com.quorum.tessera.key.vault;

public class KeyVaultException extends RuntimeException {

  public KeyVaultException(Throwable cause) {
    super(cause);
  }

  public KeyVaultException(String message) {
    super(message);
  }
}
