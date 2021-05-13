package com.quorum.tessera.encryption;

class SharedKeyImpl extends BaseKey implements SharedKey {

  SharedKeyImpl(byte[] keyBytes) {
    super(keyBytes);
  }
}
