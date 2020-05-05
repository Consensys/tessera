package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.PrivacyMode;

import javax.persistence.*;

@Entity
@Table(name = "ST_TRANSACTION_VERSION")
public class StagingTransactionVersion {

    @Id
    @GeneratedValue(generator = "ATOMIC_LONG",strategy=GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumns({@JoinColumn(name = "TXN_ID", referencedColumnName = "ID", insertable = false, updatable = false)})
    private StagingTransaction transaction;

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
        if (o == null || getClass() != o.getClass()) return false;

        StagingTransactionVersion that = (StagingTransactionVersion) o;
        if(id == null) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
