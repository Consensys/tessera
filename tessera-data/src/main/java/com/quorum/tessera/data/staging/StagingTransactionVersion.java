package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.PrivacyMode;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ST_TRANSACTION_VERSION")
public class StagingTransactionVersion {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Embedded
    @AttributeOverride(name = "hash.hash", column = @Column(name = "HASH", nullable = false, updatable = false))
    @AttributeOverride(
            name = "recipient.recBytes",
            column = @Column(name = "RECIPIENT", nullable = false, updatable = false))
    private StagingTransactionRecipientId stagingTransactionRecipientId;

    @ManyToOne
    @JoinColumns({@JoinColumn(name = "HASH", referencedColumnName = "HASH", insertable = false, updatable = false)})
    private StagingTransaction transaction;

    public StagingRecipient recipient() {
        return this.stagingTransactionRecipientId.getRecipient();
    }

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "PRIVACY_MODE", updatable = false)
    private PrivacyMode privacyMode = PrivacyMode.STANDARD_PRIVATE;

    @Lob
    @Column(name = "PAYLOAD", updatable = false, nullable = false)
    @Basic
    private byte[] payload;

    @Column(name = "TIMESTAMP", updatable = false, nullable = false)
    @Basic
    private long timestamp;

    @Column(name = "NANOTIME", updatable = false, nullable = false)
    @Basic
    private long nanotime;

    public StagingTransactionVersion() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StagingTransactionRecipientId getStagingTransactionRecipientId() {
        return stagingTransactionRecipientId;
    }

    public void setStagingTransactionRecipientId(StagingTransactionRecipientId id) {
        this.stagingTransactionRecipientId = id;
    }

    public StagingTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(StagingTransaction transaction) {
        this.transaction = transaction;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getNanotime() {
        return nanotime;
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(PrivacyMode privacyMode) {
        this.privacyMode = privacyMode;
    }

    @PrePersist
    public void onPersist() {
        this.timestamp = System.currentTimeMillis();
        this.nanotime = System.nanoTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StagingTransactionVersion)) return false;
        StagingTransactionVersion that = (StagingTransactionVersion) o;
        return Objects.equals(stagingTransactionRecipientId, that.stagingTransactionRecipientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stagingTransactionRecipientId);
    }
}
