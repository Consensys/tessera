package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.PrivacyMode;
import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class EnclaveRawPayload {

  @XmlMimeType("base64Binary")
  private byte[] encryptedPayload;

  @XmlMimeType("base64Binary")
  private byte[] encryptedKey;

  @XmlMimeType("base64Binary")
  private byte[] nonce;

  @XmlMimeType("base64Binary")
  private byte[] from;

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

  public byte[] getEncryptedPayload() {
    return encryptedPayload;
  }

  public void setEncryptedPayload(byte[] encryptedPayload) {
    this.encryptedPayload = encryptedPayload;
  }

  public byte[] getEncryptedKey() {
    return encryptedKey;
  }

  public void setEncryptedKey(byte[] encryptedKey) {
    this.encryptedKey = encryptedKey;
  }

  public byte[] getNonce() {
    return nonce;
  }

  public void setNonce(byte[] nonce) {
    this.nonce = nonce;
  }

  public byte[] getFrom() {
    return from;
  }

  public void setFrom(byte[] from) {
    this.from = from;
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

  public List<byte[]> getMandatoryRecipients() {
    return mandatoryRecipients;
  }

  public void setMandatoryRecipients(List<byte[]> mandatoryRecipients) {
    this.mandatoryRecipients = mandatoryRecipients;
  }

  public byte[] getPrivacyGroupId() {
    return privacyGroupId;
  }

  public void setPrivacyGroupId(byte[] privacyGroupId) {
    this.privacyGroupId = privacyGroupId;
  }
}
