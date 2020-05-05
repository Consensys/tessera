package com.quorum.tessera.data.staging;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "ST_AFFECTED_TRANSACTION")
public class StagingAffectedTransaction {

    @Id
    @GeneratedValue(generator = "ATOMIC_LONG",strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "TXN_ID", referencedColumnName = "ID",nullable = false)
    private StagingTransaction sourceTransaction;

    @Basic
    @Column(name = "AFFECTED_HASH", nullable = false, unique = false, updatable = false)
    private String hash;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public StagingTransaction getSourceTransaction() {
        return sourceTransaction;
    }

    public void setSourceTransaction(StagingTransaction sourceTransaction) {
        this.sourceTransaction = sourceTransaction;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StagingAffectedTransaction that = (StagingAffectedTransaction) o;
        return Objects.equals(sourceTransaction.getHash(), that.sourceTransaction.getHash())&& Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceTransaction.getHash(), hash);
    }
}
