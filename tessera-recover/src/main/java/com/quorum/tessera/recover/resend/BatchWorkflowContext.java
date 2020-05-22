package com.quorum.tessera.recover.resend;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Recipient;

public class BatchWorkflowContext {

    private int batchSize;

    private Recipient recipient;

    private PublicKey recipientKey;

    private EncryptedTransaction encryptedTransaction;

    private EncodedPayload encodedPayload;

    public EncryptedTransaction getEncryptedTransaction() {
        return encryptedTransaction;
    }

    public BatchWorkflowContext setEncryptedTransaction(EncryptedTransaction encryptedTransaction) {
        this.encryptedTransaction = encryptedTransaction;
        return this;
    }

    public EncodedPayload getEncodedPayload() {
        return encodedPayload;
    }

    public void setEncodedPayload(EncodedPayload encodedPayload) {
        this.encodedPayload = encodedPayload;
    }

    public PublicKey getRecipientKey() {
        return recipientKey;
    }

    public void setRecipientKey(PublicKey recipientKey) {
        this.recipientKey = recipientKey;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
