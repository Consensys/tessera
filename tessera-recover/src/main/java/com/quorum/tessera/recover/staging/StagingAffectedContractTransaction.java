package com.quorum.tessera.recover.staging;


import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ST_AFFECTED_TRANSACTION")
public class StagingAffectedContractTransaction {

    @EmbeddedId
    @AttributeOverride(
            name = "source.hash",
            column = @Column(name = "SOURCE_HASH", nullable = false, updatable = false))
    @AttributeOverride(
            name = "affected.hash",
            column = @Column(name = "AFFECTED_HASH", nullable = false, updatable = false))
    private StagingAffectedContractTransactionId id;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "SOURCE_HASH", referencedColumnName = "HASH")
    private StagingTransaction sourceTransaction;

    public MessageHashStr affected() {
        return this.id.getAffected();
    }

    @Lob
    @Column(name = "SECURITY_HASH", updatable = false)
    private byte[] securityHash;

    public StagingAffectedContractTransaction() {}

    public StagingAffectedContractTransactionId getId() {
        return id;
    }

    public void setId(StagingAffectedContractTransactionId id) {
        this.id = id;
    }

    public byte[] getSecurityHash() {
        return securityHash;
    }

    public void setSecurityHash(byte[] securityHash) {
        this.securityHash = securityHash;
    }

    public StagingTransaction getSourceTransaction() {
        return sourceTransaction;
    }

    public void setSourceTransaction(StagingTransaction sourceTransaction) {
        this.sourceTransaction = sourceTransaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StagingAffectedContractTransaction)) return false;
        StagingAffectedContractTransaction that = (StagingAffectedContractTransaction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
