package com.quorum.tessera.enclave;

import java.util.Arrays;

public interface RecipientBox {

  byte[] getData();

  static RecipientBox from(byte[] data) {
    return new RecipientBox() {
      @Override
      public byte[] getData() {
        return Arrays.copyOf(data, data.length);
      }

      @Override
      public int hashCode() {
        return Arrays.hashCode(data);
      }

      @Override
      public boolean equals(Object obj) {
        return Arrays.equals(data, RecipientBox.class.cast(obj).getData());
      }
    };
  }
}
