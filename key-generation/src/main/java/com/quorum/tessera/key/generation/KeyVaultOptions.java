package com.quorum.tessera.key.generation;

public class KeyVaultOptions {
  private String secretEngineName;

  public KeyVaultOptions(String secretEngineName) {
    this.secretEngineName = secretEngineName;
  }

  public String getSecretEngineName() {
    return secretEngineName;
  }
}
