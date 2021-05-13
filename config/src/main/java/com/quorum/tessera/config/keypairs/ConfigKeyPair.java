package com.quorum.tessera.config.keypairs;

public interface ConfigKeyPair {

  String getPublicKey();

  String getPrivateKey();

  void withPassword(char[] password);

  char[] getPassword();
}
