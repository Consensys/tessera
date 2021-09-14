package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.PrivacyMode;
import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
public class EnclavePayload implements Serializable {

  @XmlMimeType("base64Binary")
  private byte[] data;

  @XmlMimeType("base64Binary")
  private byte[] senderKey;

  @XmlMimeType("base64Binary")
  private List<byte[]> recipientPublicKeys;

  private PrivacyMode privacyMode;

  private List<KeyValuePair> affectedContractTransactions;

  @XmlMimeType("base64Binary")
  private byte[] execHash;

  @XmlMimeType("base64Binary")
  private List<byte[]> mandatoryRecipients;

  @XmlMimeType("base64Binary")
  private byte[] privacyGroupId;

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public byte[] getSenderKey() {
    return senderKey;
  }

  public void setSenderKey(byte[] senderKey) {
    this.senderKey = senderKey;
  }

  public List<byte[]> getRecipientPublicKeys() {
    return recipientPublicKeys;
  }

  public void setRecipientPublicKeys(List<byte[]> recipientPublicKeys) {
    this.recipientPublicKeys = recipientPublicKeys;
  }

  public PrivacyMode getPrivacyMode() {
    return privacyMode;
  }

  public void setPrivacyMode(PrivacyMode privacyMode) {
    this.privacyMode = privacyMode;
  }

  public List<KeyValuePair> getAffectedContractTransactions() {
    return affectedContractTransactions;
  }

  public void setAffectedContractTransactions(List<KeyValuePair> affectedContractTransactions) {
    this.affectedContractTransactions = affectedContractTransactions;
  }

  public byte[] getExecHash() {
    return execHash;
  }

  public void setExecHash(byte[] execHash) {
    this.execHash = execHash;
  }

  public byte[] getPrivacyGroupId() {
    return privacyGroupId;
  }

  public void setPrivacyGroupId(byte[] privacyGroupId) {
    this.privacyGroupId = privacyGroupId;
  }

  public List<byte[]> getMandatoryRecipients() {
    return mandatoryRecipients;
  }

  public void setMandatoryRecipients(List<byte[]> mandatoryRecipients) {
    this.mandatoryRecipients = mandatoryRecipients;
  }
}
