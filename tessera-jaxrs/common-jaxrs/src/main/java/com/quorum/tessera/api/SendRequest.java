package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlMimeType;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * <p>Used when a new transaction is to be created where this node is the sender
 */
public class SendRequest {

  @Schema(description = "private tx data to be encrypted", type = "string", format = "base64")
  @XmlMimeType("base64Binary")
  @Size(min = 1)
  @NotNull
  private byte[] payload;

  @Schema(
      description = "public key identifying the sender of the payload",
      type = "string",
      format = "base64")
  private String from;

  @ArraySchema(
      arraySchema = @Schema(description = "public keys identifying the recipients of the payload"),
      schema = @Schema(format = "base64"))
  private String[] to;

  @Schema(
      description =
          "the privacy mode of the transaction\n* 0 = standard private\n* 1 = party protection\n* 3 = private-state validation",
      allowableValues = {"0", "1", "3"})
  private int privacyFlag;

  @ArraySchema(
      arraySchema =
          @Schema(
              description =
                  "encoded payload hashes identifying all affected private contracts after tx simulation"),
      schema = @Schema(format = "base64"))
  private String[] affectedContractTransactions;

  @Schema(
      description = "execution hash; merkle root of all affected contracts after tx simulation",
      format = "base64")
  private String execHash;

  @Schema(description = "privacy group id of the payload", format = "base64")
  private String privacyGroupId;

  @ArraySchema(
      arraySchema =
          @Schema(description = "public keys identifying the mandatory recipients of the payload"),
      schema = @Schema(format = "base64"))
  private String[] mandatoryRecipients;

  public byte[] getPayload() {
    return this.payload;
  }

  public void setPayload(final byte[] payload) {
    this.payload = payload;
  }

  public String getFrom() {
    return this.from;
  }

  public void setFrom(final String from) {
    this.from = from;
  }

  public String[] getTo() {
    return to;
  }

  public void setTo(final String... to) {
    this.to = to;
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

  public void setAffectedContractTransactions(String... affectedContractTransactions) {
    this.affectedContractTransactions = affectedContractTransactions;
  }

  public String getExecHash() {
    return execHash;
  }

  public void setExecHash(String execHash) {
    this.execHash = execHash;
  }

  public String getPrivacyGroupId() {
    return privacyGroupId;
  }

  public void setPrivacyGroupId(String privacyGroupId) {
    this.privacyGroupId = privacyGroupId;
  }

  public String[] getMandatoryRecipients() {
    return mandatoryRecipients;
  }

  public void setMandatoryRecipients(String... mandatoryRecipients) {
    this.mandatoryRecipients = mandatoryRecipients;
  }
}
