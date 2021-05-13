package com.quorum.tessera.enclave;

import java.util.Objects;

public class AffectedTransaction {

  private final TxHash hash;

  private final EncodedPayload payload;

  private AffectedTransaction(TxHash hash, EncodedPayload payload) {
    this.hash = Objects.requireNonNull(hash);
    this.payload = Objects.requireNonNull(payload);
  }

  public TxHash getHash() {
    return hash;
  }

  public EncodedPayload getPayload() {
    return payload;
  }

  public static class Builder {

    private byte[] hash;

    private EncodedPayload payload;

    public Builder withHash(byte[] hash) {
      this.hash = hash;
      return this;
    }

    public Builder withPayload(EncodedPayload payload) {
      this.payload = payload;
      return this;
    }

    private Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public AffectedTransaction build() {
      TxHash txHash = new TxHash(hash);
      return new AffectedTransaction(txHash, payload);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AffectedTransaction that = (AffectedTransaction) o;
    return Objects.equals(hash, that.hash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash);
  }
}
