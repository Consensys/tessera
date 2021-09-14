package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlMimeType;

/**
 * Model representation of a JSON body on outgoing HTTP requests
 *
 * <p>Contains a Base64 encoded string that is the decrypting payload of a transaction
 */
public class ReceiveResponse {

  @Schema(description = "decrypted ciphertext payload", type = "string", format = "base64")
  @XmlMimeType("base64Binary")
  private byte[] payload;

  @Schema(
      description =
          "the privacy mode of the transaction\n* 0 = standard private\n* 1 = party protection\n* 3 = private-state validation",
      allowableValues = {"0", "1", "3"})
  private int privacyFlag;

  @Schema(
      description =
          "encoded payload hashes identifying all affected private contracts after tx simulation",
      format = "base64")
  private String[] affectedContractTransactions;

  @Schema(
      description = "execution hash; merkle root of all affected contracts after tx simulation",
      format = "base64")
  private String execHash;

  @Schema(
      description =
          "participant public keys of key pairs managed by the enclave of this Tessera instance",
      format = "base64")
  private String[] managedParties;

  @Schema(description = "public key of the transaction sender", format = "base64")
  private String senderKey;

  @Schema(description = "privacy group id of the transaction", format = "base64")
  private String privacyGroupId;

  public ReceiveResponse() {}

  public byte[] getPayload() {
    return payload;
  }

  public void setPayload(final byte[] payload) {
    this.payload = payload;
  }

  public int getPrivacyFlag() {
    return privacyFlag;
  }

  public void setPrivacyFlag(int privacyFlag) {
    this.privacyFlag = privacyFlag;
  }

  public String[] getAffectedContractTransactions() {
    return affectedContractTransactions;
  }

  public void setAffectedContractTransactions(String[] affectedContractTransactions) {
    this.affectedContractTransactions = affectedContractTransactions;
  }

  public String getExecHash() {
    return execHash;
  }

  public void setExecHash(String execHash) {
    this.execHash = execHash;
  }

  public String[] getManagedParties() {
    return managedParties;
  }

  public void setManagedParties(final String[] managedParties) {
    this.managedParties = managedParties;
  }

  public String getSenderKey() {
    return senderKey;
  }

  public void setSenderKey(String senderKey) {
    this.senderKey = senderKey;
  }

  public String getPrivacyGroupId() {
    return privacyGroupId;
  }

  public void setPrivacyGroupId(String privacyGroupId) {
    this.privacyGroupId = privacyGroupId;
  }
}
