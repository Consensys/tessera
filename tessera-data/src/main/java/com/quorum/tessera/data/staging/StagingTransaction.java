package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PrivacyMode;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;

/** The JPA entity that contains the staging transaction information. */
@EntityListeners(StagingTransactionListener.class)
@Entity
@Table(
    name = "ST_TRANSACTION",
    indexes = {
      @Index(name = "ST_TRANSACTION_VALSTG", columnList = "VALIDATION_STAGE"),
      @Index(name = "ST_TRANSACTION_HASH", columnList = "HASH")
    })
@NamedQueries({
  @NamedQuery(
      name = "StagingTransaction.stagingQuery",
      query =
          "select st FROM StagingTransaction st where st.validationStage is null and not exists "
              + "    (select act from StagingAffectedTransaction act  where act.sourceTransaction.hash = st.hash and  "
              + "        (select coalesce(sum(CASE WHEN ast.validationStage is Null THEN 1 else 0 END), 1) from StagingTransaction ast where ast.hash = act.hash) > 0"
              + "    )"),
  @NamedQuery(
      name = "StagingTransaction.countAll",
      query = "select count(st) from StagingTransaction st"),
  @NamedQuery(
      name = "StagingTransaction.countStaged",
      query = "select count(st) from StagingTransaction st where st.validationStage is not null"),
  @NamedQuery(
      name = "StagingTransaction.findAllOrderByStage",
      query =
          "select st from StagingTransaction st order by coalesce(st.validationStage, select max(st.validationStage)+1 from StagingTransaction st), st.hash")
})
public class StagingTransaction implements Serializable {

  @Id
  @GeneratedValue(generator = "ATOMIC_LONG", strategy = GenerationType.AUTO)
  @Column(name = "ID")
  private Long id;

  @Basic
  @Column(name = "HASH", nullable = false, updatable = false)
  private String hash;

  @Column(name = "PRIVACY_MODE", updatable = false)
  @Enumerated(EnumType.ORDINAL)
  private PrivacyMode privacyMode = PrivacyMode.STANDARD_PRIVATE;

  @Column(name = "VALIDATION_STAGE")
  @Basic
  private Long validationStage;

  @Column(name = "TIMESTAMP", updatable = false)
  private long timestamp;

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.ALL},
      mappedBy = "sourceTransaction",
      orphanRemoval = true)
  private Set<StagingAffectedTransaction> affectedContractTransactions = new HashSet<>();

  @Lob
  @Column(name = "PAYLOAD")
  private byte[] payload;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "PAYLOAD_CODEC", length = 50)
  private EncodedPayloadCodec encodedPayloadCodec;

  @Transient private EncodedPayload encodedPayload;

  public StagingTransaction() {}

  @PrePersist
  public void onPersist() {
    this.timestamp = System.currentTimeMillis();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getHash() {
    return this.hash;
  }

  public void setHash(final String hash) {
    this.hash = hash;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public Set<StagingAffectedTransaction> getAffectedContractTransactions() {
    return affectedContractTransactions;
  }

  public void setAffectedContractTransactions(
      Set<StagingAffectedTransaction> affectedContractTransactions) {
    this.affectedContractTransactions = affectedContractTransactions;
  }

  public Long getValidationStage() {
    return validationStage;
  }

  public void setValidationStage(Long validationStage) {
    this.validationStage = validationStage;
  }

  public PrivacyMode getPrivacyMode() {
    return privacyMode;
  }

  public void setPrivacyMode(PrivacyMode privacyMode) {
    this.privacyMode = privacyMode;
  }

  public byte[] getPayload() {
    return payload;
  }

  public void setPayload(byte[] payload) {
    this.payload = payload;
  }

  public EncodedPayloadCodec getEncodedPayloadCodec() {
    return encodedPayloadCodec;
  }

  public void setEncodedPayloadCodec(EncodedPayloadCodec encodedPayloadCodec) {
    this.encodedPayloadCodec = encodedPayloadCodec;
  }

  public EncodedPayload getEncodedPayload() {
    return encodedPayload;
  }

  public void setEncodedPayload(EncodedPayload encodedPayload) {
    this.encodedPayload = encodedPayload;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (id == null) return false;
    StagingTransaction that = (StagingTransaction) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
