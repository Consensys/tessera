package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.util.Set;

public class BatchWorkflowContext {

  private long expectedTotal;

  private int batchSize;

  private Recipient recipient;

  private PublicKey recipientKey;

  private EncryptedTransaction encryptedTransaction;

  private EncodedPayload encodedPayload;

  private Set<EncodedPayload> payloadsToPublish;

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

  public long getExpectedTotal() {
    return expectedTotal;
  }

  public void setExpectedTotal(long expectedTotal) {
    this.expectedTotal = expectedTotal;
  }

  public Set<EncodedPayload> getPayloadsToPublish() {
    return payloadsToPublish;
  }

  public void setPayloadsToPublish(Set<EncodedPayload> payloadsToPublish) {
    this.payloadsToPublish = payloadsToPublish;
  }
}
