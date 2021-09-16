package com.quorum.tessera.api;

import com.quorum.tessera.config.adapters.MapAdapter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PayloadDecryptRequest {

  @Schema(
      description = "public key identifying the sender of the payload",
      type = "string",
      format = "base64")
  @XmlElement
  @XmlMimeType("base64Binary")
  private byte[] senderKey;

  @Schema(description = "data to be decrypted", type = "string", format = "base64")
  @XmlElement
  @XmlMimeType("base64Binary")
  private byte[] cipherText;

  @Schema(description = "nonce to use in data decryption", type = "string", format = "base64")
  @XmlElement
  @XmlMimeType("base64Binary")
  private byte[] cipherTextNonce;

  @ArraySchema(
      arraySchema =
          @Schema(
              description =
                  "master key used in data decryption, wrapped for each payload recipient"),
      schema = @Schema(type = "string", format = "base64"))
  @XmlElement
  @XmlMimeType("base64Binary")
  private List<byte[]> recipientBoxes = new ArrayList<>();

  @Schema(description = "nonce to use to unwrap master key", type = "string", format = "base64")
  @XmlElement
  @XmlMimeType("base64Binary")
  private byte[] recipientNonce;

  @ArraySchema(
      arraySchema = @Schema(description = "public keys identifying each recipient of the payload"),
      schema = @Schema(type = "string", format = "base64"))
  @XmlElement
  @XmlMimeType("base64Binary")
  private List<byte[]> recipientKeys = new ArrayList<>();

  @Schema(
      description =
          "the privacy mode of the transaction\n* 0 = standard private\n* 1 = party protection\n* 3 = private-state validation",
      allowableValues = {"0", "1", "3"})
  @XmlElement
  private int privacyMode;

  @Schema(
      description =
          "mapping of encoded payload hashes (base64 encoded) to security hashes (base64 encoded)",
      format = "Map<TxHash, SecurityHash>")
  @XmlElement
  @XmlJavaTypeAdapter(MapAdapter.class)
  private Map<String, String> affectedContractTransactions = new HashMap<>();

  @Schema(
      description = "execution hash; merkle root of all affected contracts after tx simulation",
      type = "string",
      format = "base64")
  @XmlElement
  @XmlMimeType("base64Binary")
  private byte[] execHash = new byte[0];

  public byte[] getSenderKey() {
    return senderKey;
  }

  public void setSenderKey(final byte[] senderKey) {
    this.senderKey = senderKey;
  }

  public byte[] getCipherText() {
    return cipherText;
  }

  public void setCipherText(final byte[] cipherText) {
    this.cipherText = cipherText;
  }

  public byte[] getCipherTextNonce() {
    return cipherTextNonce;
  }

  public void setCipherTextNonce(final byte[] cipherTextNonce) {
    this.cipherTextNonce = cipherTextNonce;
  }

  public List<byte[]> getRecipientBoxes() {
    return recipientBoxes;
  }

  public void setRecipientBoxes(final List<byte[]> recipientBoxes) {
    this.recipientBoxes = recipientBoxes;
  }

  public byte[] getRecipientNonce() {
    return recipientNonce;
  }

  public void setRecipientNonce(final byte[] recipientNonce) {
    this.recipientNonce = recipientNonce;
  }

  public List<byte[]> getRecipientKeys() {
    return recipientKeys;
  }

  public void setRecipientKeys(final List<byte[]> recipientKeys) {
    this.recipientKeys = recipientKeys;
  }

  public int getPrivacyMode() {
    return privacyMode;
  }

  public void setPrivacyMode(final int privacyMode) {
    this.privacyMode = privacyMode;
  }

  public Map<String, String> getAffectedContractTransactions() {
    return affectedContractTransactions;
  }

  public void setAffectedContractTransactions(
      final Map<String, String> affectedContractTransactions) {
    this.affectedContractTransactions = affectedContractTransactions;
  }

  public byte[] getExecHash() {
    return execHash;
  }

  public void setExecHash(final byte[] execHash) {
    this.execHash = execHash;
  }
}
