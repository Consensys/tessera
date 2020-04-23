package com.quorum.tessera.recover.staging;


import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StagingAffectedContractTransactionId implements Serializable {

    private MessageHashStr source;
    private MessageHashStr affected;

    public StagingAffectedContractTransactionId() {}

    public StagingAffectedContractTransactionId(MessageHashStr source, MessageHashStr affected) {
        this.source = source;
        this.affected = affected;
    }

    public MessageHashStr getSource() {
        return source;
    }

    public void setSource(MessageHashStr source) {
        this.source = source;
    }

    public MessageHashStr getAffected() {
        return affected;
    }

    public void setAffected(MessageHashStr affected) {
        this.affected = affected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StagingAffectedContractTransactionId)) return false;
        StagingAffectedContractTransactionId that = (StagingAffectedContractTransactionId) o;
        return Objects.equals(source, that.source) && Objects.equals(affected, that.affected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, affected);
    }
}
