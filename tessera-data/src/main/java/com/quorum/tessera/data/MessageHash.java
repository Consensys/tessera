package com.quorum.tessera.data;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;

@Embeddable
public class MessageHash implements Serializable {

  @Lob private byte[] hashBytes;

  public MessageHash() {}

  public MessageHash(final byte[] hashBytes) {
    this.hashBytes = Arrays.copyOf(hashBytes, hashBytes.length);
  }

  public void setHashBytes(final byte[] hashBytes) {
    this.hashBytes = Arrays.copyOf(hashBytes, hashBytes.length);
  }

  public byte[] getHashBytes() {
    return Arrays.copyOf(hashBytes, hashBytes.length);
  }

  @Override
  public boolean equals(final Object o) {
    return (o instanceof MessageHash) && Arrays.equals(hashBytes, ((MessageHash) o).hashBytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getHashBytes());
  }

  // FIXME: toString is being used as a message format thing used to messages
  // rather than being a string representation of the object.
  @Override
  public String toString() {
    return Base64.getEncoder().encodeToString(hashBytes);
  }
}
