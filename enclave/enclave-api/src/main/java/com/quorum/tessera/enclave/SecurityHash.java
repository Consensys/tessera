package com.quorum.tessera.enclave;

import java.util.Arrays;

public interface SecurityHash {

  byte[] getData();

  static SecurityHash from(byte[] data) {

    return new SecurityHash() {
      @Override
      public byte[] getData() {
        return data;
      }

      @Override
      public int hashCode() {
        return Arrays.hashCode(data);
      }

      @Override
      public boolean equals(Object obj) {
        return Arrays.equals(data, SecurityHash.class.cast(obj).getData());
      }
    };
  }
}
