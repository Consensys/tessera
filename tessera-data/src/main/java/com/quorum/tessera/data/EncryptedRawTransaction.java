package com.quorum.tessera.data;

import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/** The JPA entity that contains the raw transaction information */
@NamedQueries({
  @NamedQuery(
      name = "EncryptedRawTransaction.DeleteByHash",
      query = "delete from EncryptedRawTransaction where hash.hashBytes = :hash"),
  @NamedQuery(
      name = "EncryptedRawTransaction.Upcheck",
      query = "select count(c.timestamp) from EncryptedRawTransaction c"),
  @NamedQuery(
      name = "EncryptedRawTransaction.FindAll",
      query = "select ert from EncryptedRawTransaction ert order by ert.timestamp, ert.hash"),
})
@Entity
@Table(name = "ENCRYPTED_RAW_TRANSACTION")
public class EncryptedRawTransaction implements Serializable {

  @EmbeddedId
  @AttributeOverride(
      name = "hashBytes",
      column = @Column(name = "HASH", nullable = false, unique = true, updatable = false))
  private MessageHash hash;

  @Lob
  @Column(name = "ENCRYPTED_PAYLOAD", nullable = false)
  private byte[] encryptedPayload;

  @Lob
  @Column(name = "ENCRYPTED_KEY", nullable = false)
  private byte[] encryptedKey;

  @Lob
  @Column(name = "NONCE", nullable = false)
  private byte[] nonce;

  @Lob
  @Column(name = "SENDER", nullable = false)
  private byte[] sender;

  @Column(name = "TIMESTAMP", updatable = false)
  private long timestamp;

  public EncryptedRawTransaction(
      final MessageHash hash,
      final byte[] encryptedPayload,
      final byte[] encryptedKey,
      final byte[] nonce,
      final byte[] sender) {
    this.hash = hash;
    this.encryptedPayload = encryptedPayload;
    this.encryptedKey = encryptedKey;
    this.nonce = nonce;
    this.sender = sender;
  }

  public EncryptedRawTransaction() {}

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

  public byte[] getEncryptedPayload() {
    return encryptedPayload;
  }

  public void setEncryptedPayload(byte[] encryptedPayload) {
    this.encryptedPayload = encryptedPayload;
  }

  public byte[] getEncryptedKey() {
    return encryptedKey;
  }

  public void setEncryptedKey(byte[] encryptedKey) {
    this.encryptedKey = encryptedKey;
  }

  public byte[] getNonce() {
    return nonce;
  }

  public void setNonce(byte[] nonce) {
    this.nonce = nonce;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public byte[] getSender() {
    return sender;
  }

  public void setSender(byte[] sender) {
    this.sender = sender;
  }

  @Override
  public int hashCode() {
    return 47 * 3 + Objects.hashCode(this.hash);
  }

  @Override
  public boolean equals(final Object obj) {

    return (obj instanceof EncryptedRawTransaction)
        && Objects.equals(this.hash, ((EncryptedRawTransaction) obj).hash);
  }

  public RawTransaction toRawTransaction() {
    return new RawTransaction(
        this.encryptedPayload,
        this.encryptedKey,
        new Nonce(this.nonce),
        PublicKey.from(this.sender));
  }
}
