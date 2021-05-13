package com.quorum.tessera.encryption;

public interface PrivateKey extends Key {

  static PrivateKey from(byte[] data) {
    return new PrivateKeyImpl(data);
  }
}
