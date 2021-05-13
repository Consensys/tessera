package com.quorum.tessera.config;

import com.quorum.tessera.config.keypairs.AWSKeyPair;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;

public enum KeyVaultType {
  AZURE(AzureVaultKeyPair.class),
  HASHICORP(HashicorpVaultKeyPair.class),
  AWS(AWSKeyPair.class);

  private Class<? extends ConfigKeyPair> keyPairType;

  KeyVaultType(Class<? extends ConfigKeyPair> keyPairType) {
    this.keyPairType = keyPairType;
  }

  public Class<? extends ConfigKeyPair> getKeyPairType() {
    return keyPairType;
  }
}
