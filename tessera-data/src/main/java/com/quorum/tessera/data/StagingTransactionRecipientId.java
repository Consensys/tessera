package com.quorum.tessera.data;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StagingTransactionRecipientId implements Serializable {

    private MessageHashStr hash;
    private StagingRecipient recipient;

    public StagingTransactionRecipientId(){
    }

    public StagingTransactionRecipientId(MessageHashStr hash, StagingRecipient recipient) {
        this.hash = Objects.requireNonNull(hash, "RecipientID.hash must not be null");
        this.recipient = Objects.requireNonNull(recipient, "RecipientID.recipient must not be null");
    }

    public MessageHashStr getHash() {
        return hash;
    }

    public void setHash(MessageHashStr hash) {
        this.hash = hash;
    }

    public StagingRecipient getRecipient() {
        return recipient;
    }

    public void setRecipient(StagingRecipient recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StagingTransactionRecipientId)) return false;
        StagingTransactionRecipientId that = (StagingTransactionRecipientId) o;
        return hash.equals(that.hash) &&
            recipient.equals(that.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, recipient);
    }
}
