package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

public class AzureSetSecretData implements SetSecretData {
  private String secretName;

  private String secret;

  public AzureSetSecretData(String secretName, String secret) {
    this.secretName = secretName;
    this.secret = secret;
  }

  public String getSecretName() {
    return secretName;
  }

  public String getSecret() {
    return secret;
  }

  @Override
  public KeyVaultType getType() {
    return KeyVaultType.AZURE;
  }
}
