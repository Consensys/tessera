package com.quorum.tessera.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlMimeType;

/**
 * Model representation of a JSON body on outgoing HTTP requests
 *
 * <p>Contains a Base64 encoded string that is the decrypting payload of a transaction
 */
public class BesuReceiveResponse {

  @Schema(description = "decrypted ciphertext payload", type = "string", format = "base64")
  @XmlMimeType("base64Binary")
  private byte[] payload;

  @Schema(description = "public key of the transaction sender", format = "base64")
  private String senderKey;

  @Schema(description = "privacy group id of the transaction", format = "base64")
  private String privacyGroupId;

  public BesuReceiveResponse() {}

  public byte[] getPayload() {
    return payload;
  }

  public void setPayload(final byte[] payload) {
    this.payload = payload;
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
