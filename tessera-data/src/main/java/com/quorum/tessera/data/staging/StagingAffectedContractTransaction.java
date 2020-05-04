package com.quorum.tessera.data.staging;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ST_AFFECTED_TRANSACTION")
public class StagingAffectedContractTransaction {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Embedded
    @AttributeOverride(
            name = "source.hash",
            column = @Column(name = "SOURCE_HASH", nullable = false, updatable = false))
    @AttributeOverride(
            name = "affected.hash",
            column = @Column(name = "AFFECTED_HASH", nullable = false, updatable = false))
    private StagingAffectedContractTransactionId stagingAffectedContractTransactionId;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "SOURCE_HASH", referencedColumnName = "HASH")
    private StagingTransaction sourceTransaction;

    public MessageHashStr affected() {
        return this.stagingAffectedContractTransactionId.getAffected();
    }

    @Lob
    @Column(name = "SECURITY_HASH", updatable = false)
    private byte[] securityHash;

    public StagingAffectedContractTransaction() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StagingAffectedContractTransactionId getStagingAffectedContractTransactionId() {
        return stagingAffectedContractTransactionId;
    }

    public void setStagingAffectedContractTransactionId(StagingAffectedContractTransactionId stagingAffectedContractTransactionId) {
        this.stagingAffectedContractTransactionId = stagingAffectedContractTransactionId;
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
        return stagingAffectedContractTransactionId.equals(that.stagingAffectedContractTransactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stagingAffectedContractTransactionId);
    }
}
