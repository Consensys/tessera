package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

public class AzureGetSecretData implements GetSecretData {

  private String secretName;

  private String secretVersion;

  public AzureGetSecretData(String secretName, String secretVersion) {
    this.secretName = secretName;
    this.secretVersion = secretVersion;
  }

  @Override
  public KeyVaultType getType() {
    return KeyVaultType.AZURE;
  }

  public String getSecretName() {
    return secretName;
  }

  public String getSecretVersion() {
    return secretVersion;
  }
}
