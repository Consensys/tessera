package com.quorum.tessera.data;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * The JPA entity that contains the transaction information A simple key/value pair
 *
 * <p>The key is the transaction hash, and the value an a fully encoded {@link
 * com.quorum.tessera.enclave.EncodedPayload}
 */
@NamedQueries({
  @NamedQuery(
      name = "EncryptedTransaction.FindByHash",
      query = "SELECT et FROM EncryptedTransaction et WHERE et.hash.hashBytes = :hash"),
  @NamedQuery(
      name = "EncryptedTransaction.FindAll",
      query = "select et from EncryptedTransaction et order by et.timestamp,et.hash"),
  @NamedQuery(
      name = "EncryptedTransaction.Upcheck",
      query = "select count(c.timestamp) from EncryptedTransaction c")
})
@Entity
@EntityListeners(EncryptedTransactionListener.class)
@Table(name = "ENCRYPTED_TRANSACTION")
public class EncryptedTransaction implements Serializable {

  @EmbeddedId
  @AttributeOverride(
      name = "hashBytes",
      column = @Column(name = "HASH", nullable = false, unique = true, updatable = false))
  private MessageHash hash;

  @Lob
  @Column(name = "ENCODED_PAYLOAD", nullable = false)
  private byte[] encodedPayload;

  @Column(name = "TIMESTAMP", updatable = false)
  private long timestamp;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "PAYLOAD_CODEC", nullable = false, length = 50)
  private EncodedPayloadCodec encodedPayloadCodec;

  @Transient private transient EncodedPayload payload;

  /*
  TODO: Use empty consructor jpa entities are intentially mutable and this constructor will make adding new properties trickier
   */
  @Deprecated
  public EncryptedTransaction(final MessageHash hash, final byte[] encodedPayload) {
    this.hash = hash;
    this.encodedPayload = encodedPayload;
  }

  public EncryptedTransaction() {}

  @PrePersist
  public void onPersist() {
    this.timestamp = System.currentTimeMillis();
  }

  public MessageHash getHash() {
    return this.hash;
  }

  public void setHash(final MessageHash hash) {
    this.hash = hash;
  }

  public byte[] getEncodedPayload() {
    return this.encodedPayload;
  }

  public void setEncodedPayload(final byte[] encodedPayload) {
    this.encodedPayload = encodedPayload;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public EncodedPayloadCodec getEncodedPayloadCodec() {
    return encodedPayloadCodec;
  }

  public void setEncodedPayloadCodec(EncodedPayloadCodec encodedPayloadCodec) {
    this.encodedPayloadCodec = encodedPayloadCodec;
  }

  public EncodedPayload getPayload() {
    return payload;
  }

  public void setPayload(EncodedPayload payload) {
    this.payload = payload;
  }

  @Override
  public int hashCode() {
    return 47 * 3 + Objects.hashCode(this.hash);
  }

  @Override
  public boolean equals(final Object obj) {

    return (obj instanceof EncryptedTransaction)
        && Objects.equals(this.hash, ((EncryptedTransaction) obj).hash);
  }

  @Override
  public String toString() {
    return "EncryptedTransaction{"
        + "hash="
        + hash
        + ", encodedPayloadCodec="
        + encodedPayloadCodec
        + '}';
  }
}
