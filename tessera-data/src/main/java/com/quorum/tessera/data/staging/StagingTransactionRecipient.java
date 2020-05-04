package com.quorum.tessera.data.staging;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ST_TRANSACTION_RECIPIENT")
public class StagingTransactionRecipient {

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

    @Basic private boolean initiator;

    @Lob
    @Column(name = "BOX", updatable = false)
    @Basic
    private byte[] box;

    public StagingTransactionRecipient() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StagingTransactionRecipientId getStagingTransactionRecipientId() {
        return stagingTransactionRecipientId;
    }

    public void setStagingTransactionRecipientId(StagingTransactionRecipientId stagingTransactionRecipientId) {
        this.stagingTransactionRecipientId = stagingTransactionRecipientId;
    }

    public StagingTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(StagingTransaction transaction) {
        this.transaction = transaction;
    }

    public boolean isInitiator() {
        return initiator;
    }

    public void setInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    public byte[] getBox() {
        return box;
    }

    public void setBox(byte[] box) {
        this.box = box;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StagingTransactionRecipient)) return false;
        StagingTransactionRecipient that = (StagingTransactionRecipient) o;
        return Objects.equals(stagingTransactionRecipientId, that.stagingTransactionRecipientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stagingTransactionRecipientId);
    }
}
