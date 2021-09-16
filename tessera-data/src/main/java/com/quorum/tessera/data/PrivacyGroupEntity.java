package com.quorum.tessera.data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Arrays;

/** The JPA entity that contains the privacy group information */
@NamedQueries({
  @NamedQuery(
      name = "PrivacyGroup.FindById",
      query = "SELECT pg FROM PrivacyGroupEntity pg WHERE pg.id = :id"),
  @NamedQuery(
      name = "PrivacyGroup.FindByLookupId",
      query = "select pg from PrivacyGroupEntity pg WHERE pg.lookupId = :lookupId"),
  @NamedQuery(name = "PrivacyGroup.FindAll", query = "select pg from PrivacyGroupEntity pg")
})
@Entity
@Table(
    name = "PRIVACY_GROUP",
    indexes = {@Index(name = "PRIVACY_GROUP_LOOKUPID", columnList = "LOOKUP_ID")})
public class PrivacyGroupEntity implements Serializable {

  @Id
  @Column(name = "ID")
  private byte[] id;

  @Lob
  @Column(name = "LOOKUP_ID")
  private byte[] lookupId;

  @Lob
  @Column(name = "DATA", nullable = false)
  private byte[] data;

  @Column(name = "TIMESTAMP", updatable = false)
  private long timestamp;

  public PrivacyGroupEntity(final byte[] id, final byte[] lookupId, final byte[] data) {
    this.id = id;
    this.lookupId = lookupId;
    this.data = data;
  }

  public PrivacyGroupEntity() {}

  @PrePersist
  public void onPersist() {
    this.timestamp = System.currentTimeMillis();
  }

  public byte[] getId() {
    return id;
  }

  public void setId(byte[] id) {
    this.id = id;
  }

  public byte[] getLookupId() {
    return lookupId;
  }

  public void setLookupId(byte[] lookupId) {
    this.lookupId = lookupId;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof PrivacyGroupEntity) && Arrays.equals(id, ((PrivacyGroupEntity) obj).id);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(id);
  }
}
