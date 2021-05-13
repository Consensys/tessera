package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Objects;

public class RawTransaction {

  private final byte[] encryptedPayload;

  private final byte[] encryptedKey;

  private final Nonce nonce;

  private final PublicKey from;

  public RawTransaction(
      final byte[] encryptedPayload,
      final byte[] encryptedKey,
      final Nonce nonce,
      final PublicKey from) {
    this.encryptedPayload = Objects.requireNonNull(encryptedPayload);
    this.encryptedKey = Objects.requireNonNull(encryptedKey);
    this.nonce = Objects.requireNonNull(nonce);
    this.from = Objects.requireNonNull(from);
  }

  public byte[] getEncryptedPayload() {
    return encryptedPayload;
  }

  public byte[] getEncryptedKey() {
    return encryptedKey;
  }

  public Nonce getNonce() {
    return nonce;
  }

  public PublicKey getFrom() {
    return from;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 61 * hash + Arrays.hashCode(this.encryptedPayload);
    hash = 61 * hash + Arrays.hashCode(this.encryptedKey);
    hash = 61 * hash + Objects.hashCode(this.nonce);
    hash = 61 * hash + Objects.hashCode(this.from);
    return hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof RawTransaction)) {
      return false;
    }

    final RawTransaction other = (RawTransaction) obj;

    return Objects.equals(this.nonce, other.nonce)
        && Objects.equals(this.from, other.from)
        && Arrays.equals(this.encryptedKey, other.encryptedKey)
        && Arrays.equals(this.encryptedPayload, other.encryptedPayload);
  }
}
