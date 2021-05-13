package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

public class AWSGetSecretData implements GetSecretData {
  private final String secretName;

  public AWSGetSecretData(String secretName) {
    this.secretName = secretName;
  }

  @Override
  public KeyVaultType getType() {
    return KeyVaultType.AWS;
  }

  public String getSecretName() {
    return secretName;
  }
}
