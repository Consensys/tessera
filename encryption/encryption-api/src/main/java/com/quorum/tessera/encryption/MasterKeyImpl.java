package com.quorum.tessera.encryption;

public class MasterKeyImpl extends BaseKey implements MasterKey {

  MasterKeyImpl(byte[] keyBytes) {
    super(keyBytes);
  }
}
