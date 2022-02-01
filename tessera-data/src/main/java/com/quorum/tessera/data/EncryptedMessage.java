package com.quorum.tessera.data;

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
      name = "EncryptedMessage.FindByHash",
      query = "SELECT em FROM EncryptedMessage em WHERE em.hash.hashBytes = :hash"),
  @NamedQuery(
      name = "EncryptedMessage.FindAll",
      query = "select em from EncryptedMessage em order by em.timestamp, em.hash"),
  @NamedQuery(
      name = "EncryptedMessage.FindAllHashes",
      query = "select em.hash from EncryptedMessage em order by em.timestamp, em.hash"),
  @NamedQuery(
      name = "EncryptedMessage.Upcheck",
      query = "select count(em) from EncryptedMessage em")
})
@Entity
@Table(name = "ENCRYPTED_MESSAGE")
public class EncryptedMessage implements Serializable {

  @EmbeddedId
  @AttributeOverride(
      name = "hashBytes",
      column = @Column(name = "HASH", nullable = false, unique = true, updatable = false))
  private MessageHash hash;

  @Lob
  @Column(name = "CONTENT", nullable = false)
  private byte[] content;

  /*
  @Lob
  @Column(name = "FROM", nullable = false, updatable = false)
  private byte[] from;

  @Lob
  @Column(name = "TO", nullable = false, updatable = false)
  private byte[] to;
  */

  @Column(name = "TIMESTAMP", updatable = true)
  private long timestamp;

  public EncryptedMessage(final MessageHash hash, final byte[] content) {
    this.hash = hash;
    this.content = content;
  }

  public EncryptedMessage() {}

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

  public byte[] getContent() {
    return this.content;
  }

  public void setContent(final byte[] content) {
    this.content = content;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public int hashCode() {
    return 47 * 3 + Objects.hashCode(this.hash);
  }

  @Override
  public boolean equals(final Object obj) {

    return (obj instanceof EncryptedMessage)
        && Objects.equals(this.hash, ((EncryptedMessage) obj).hash);
  }
}
