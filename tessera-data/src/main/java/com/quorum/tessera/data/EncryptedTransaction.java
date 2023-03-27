package com.quorum.tessera.data;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import jakarta.persistence.*;
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
      query = "select et from EncryptedTransaction et where et.hash.hashBytes = :hash"),
  @NamedQuery(
      name = "EncryptedTransaction.FindAll",
      query = "select et from EncryptedTransaction et order by et.timestamp,et.hash"),
  @NamedQuery(
      name = "EncryptedTransaction.Upcheck",
      query = "select count(c) from EncryptedTransaction c where 1 = 2")
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

  @Enumerated(EnumType.STRING)
  @Column(name = "PAYLOAD_CODEC", length = 50)
  private EncodedPayloadCodec encodedPayloadCodec;

  @Column(name = "TIMESTAMP", updatable = false)
  private long timestamp;

  @Transient private transient EncodedPayload payload;

  public EncryptedTransaction(final MessageHash hash, final EncodedPayload payload) {
    this.hash = hash;
    this.payload = payload;
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

  public long getTimestamp() {
    return this.timestamp;
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
}
