package com.quorum.tessera.recover.staging;

import com.quorum.tessera.data.staging.StagingRecipient;
import com.quorum.tessera.data.staging.StagingTransactionRecipientId;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ST_TRANSACTION_RECIPIENT")
public class StagingTransactionRecipient {

    @EmbeddedId
    @AttributeOverride(name = "hash.hash", column = @Column(name = "HASH", nullable = false, updatable = false))
    @AttributeOverride(
            name = "recipient.recBytes",
            column = @Column(name = "RECIPIENT", nullable = false, updatable = false))
    private StagingTransactionRecipientId id;

    @ManyToOne
    @JoinColumns({@JoinColumn(name = "HASH", referencedColumnName = "HASH", insertable = false, updatable = false)})
    private StagingTransaction transaction;

    public StagingRecipient recipient() {
        return this.id.getRecipient();
    }

    @Basic private boolean initiator;

    @Lob
    @Column(name = "BOX", updatable = false)
    @Basic
    private byte[] box;

    public StagingTransactionRecipient() {}

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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
