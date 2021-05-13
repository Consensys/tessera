package com.quorum.tessera.encryption;

class PrivateKeyImpl extends BaseKey implements PrivateKey {

  PrivateKeyImpl(byte[] keyBytes) {
    super(keyBytes);
  }
}
