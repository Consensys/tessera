package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.keypairs.*;

public class GeneratedKeyPair {
  // key pair data that can be marshalled and used to update the configfile
  private ConfigKeyPair configKeyPair;

  private String publicKey;

  public GeneratedKeyPair(ConfigKeyPair configKeyPair, String publicKey) {
    this.configKeyPair = configKeyPair;
    this.publicKey = publicKey;
  }

  public ConfigKeyPair getConfigKeyPair() {
    return configKeyPair;
  }

  public String getPublicKey() {
    return publicKey;
  }
}
