package com.quorum.tessera.data;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ST_TRANSACTION_VERSION")
public class StagingTransactionVersion {

    @EmbeddedId
    @AttributeOverride(
        name = "hash.hash",
        column = @Column(name = "HASH", nullable = false, updatable = false)
    )
    @AttributeOverride(
        name = "recipient.recBytes",
        column = @Column(name = "RECIPIENT", nullable = false, updatable = false)
    )
    private StagingTransactionRecipientId id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "HASH", referencedColumnName = "HASH", insertable = false, updatable = false)
    })
    private StagingTransaction transaction;

    public StagingRecipient recipient(){
        return this.id.getRecipient();
    }

    @Column(name = "PRIVACY_MODE", updatable = false)
    @Basic
    private byte privacyMode;

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

    public StagingTransactionVersion() {
    }

    public StagingTransactionRecipientId getId() {
        return id;
    }

    public void setId(StagingTransactionRecipientId id) {
        this.id = id;
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

    public byte getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(byte privacyMode) {
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
