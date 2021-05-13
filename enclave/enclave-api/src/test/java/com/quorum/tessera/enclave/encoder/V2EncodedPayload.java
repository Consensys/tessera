package com.quorum.tessera.enclave.encoder;

import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.enclave.SecurityHash;
import com.quorum.tessera.enclave.TxHash;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

/** This class contains the data that is sent to other nodes */
public class V2EncodedPayload {

  private final PublicKey senderKey;

  private final byte[] cipherText;

  private final Nonce cipherTextNonce;

  private final List<RecipientBox> recipientBoxes;

  private final Nonce recipientNonce;

  private final List<PublicKey> recipientKeys;

  private final PrivacyMode privacyMode;

  private final Map<TxHash, SecurityHash> affectedContractTransactions;

  private final byte[] execHash;

  private V2EncodedPayload(
      final PublicKey senderKey,
      final byte[] cipherText,
      final Nonce cipherTextNonce,
      final List<RecipientBox> recipientBoxes,
      final Nonce recipientNonce,
      final List<PublicKey> recipientKeys,
      final PrivacyMode privacyMode,
      final Map<TxHash, SecurityHash> affectedContractTransactions,
      final byte[] execHash) {
    this.senderKey = senderKey;
    this.cipherText = cipherText;
    this.cipherTextNonce = cipherTextNonce;
    this.recipientNonce = recipientNonce;
    this.recipientBoxes = recipientBoxes;
    this.recipientKeys = recipientKeys;
    this.privacyMode = privacyMode;
    this.affectedContractTransactions = affectedContractTransactions;
    this.execHash = execHash;
  }

  public PublicKey getSenderKey() {
    return senderKey;
  }

  public byte[] getCipherText() {
    return cipherText;
  }

  public Nonce getCipherTextNonce() {
    return cipherTextNonce;
  }

  public List<RecipientBox> getRecipientBoxes() {
    return Collections.unmodifiableList(recipientBoxes);
  }

  public Nonce getRecipientNonce() {
    return recipientNonce;
  }

  public List<PublicKey> getRecipientKeys() {
    return recipientKeys;
  }

  public PrivacyMode getPrivacyMode() {
    return privacyMode;
  }

  public Map<TxHash, SecurityHash> getAffectedContractTransactions() {
    return Collections.unmodifiableMap(affectedContractTransactions);
  }

  public byte[] getExecHash() {
    return execHash;
  }

  public static class Builder {

    private Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public static Builder from(V2EncodedPayload encodedPayload) {

      final Map<TxHash, byte[]> affectedContractTransactionMap =
          encodedPayload.getAffectedContractTransactions().entrySet().stream()
              .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getData()));

      return create()
          .withPrivacyMode(encodedPayload.getPrivacyMode())
          .withSenderKey(encodedPayload.getSenderKey())
          .withRecipientNonce(encodedPayload.getRecipientNonce())
          .withRecipientKeys(encodedPayload.getRecipientKeys())
          .withRecipientBoxes(
              encodedPayload.getRecipientBoxes().stream()
                  .map(RecipientBox::getData)
                  .collect(Collectors.toList()))
          .withPrivacyMode(encodedPayload.getPrivacyMode())
          .withExecHash(encodedPayload.getExecHash())
          .withCipherText(encodedPayload.getCipherText())
          .withCipherTextNonce(encodedPayload.getCipherTextNonce())
          .withAffectedContractTransactions(affectedContractTransactionMap);
    }

    private PublicKey senderKey;

    private byte[] cipherText;

    private Nonce cipherTextNonce;

    private Nonce recipientNonce;

    private List<byte[]> recipientBoxes = new ArrayList<>();

    private List<PublicKey> recipientKeys = new ArrayList<>();

    private PrivacyMode privacyMode = PrivacyMode.STANDARD_PRIVATE;

    private Map<TxHash, byte[]> affectedContractTransactions = Collections.emptyMap();

    private byte[] execHash = new byte[0];

    public Builder withSenderKey(final PublicKey senderKey) {
      this.senderKey = senderKey;
      return this;
    }

    public Builder withCipherText(final byte[] cipherText) {
      this.cipherText = cipherText;
      return this;
    }

    public Builder withRecipientKey(PublicKey publicKey) {
      this.recipientKeys.add(publicKey);
      return this;
    }

    public Builder withRecipientKeys(final List<PublicKey> recipientKeys) {
      this.recipientKeys.addAll(recipientKeys);
      return this;
    }

    public Builder withNewRecipientKeys(final List<PublicKey> recipientKeys) {
      this.recipientKeys = recipientKeys;
      return this;
    }

    public Builder withCipherTextNonce(final Nonce cipherTextNonce) {
      this.cipherTextNonce = cipherTextNonce;
      return this;
    }

    public Builder withCipherTextNonce(final byte[] cipherTextNonce) {
      this.cipherTextNonce = new Nonce(cipherTextNonce);
      return this;
    }

    public Builder withRecipientNonce(final Nonce recipientNonce) {
      this.recipientNonce = recipientNonce;
      return this;
    }

    public Builder withRecipientNonce(final byte[] recipientNonce) {
      this.recipientNonce = new Nonce(recipientNonce);
      return this;
    }

    public Builder withRecipientBoxes(final List<byte[]> recipientBoxes) {
      this.recipientBoxes = recipientBoxes;
      return this;
    }

    public Builder withRecipientBox(byte[] newbox) {
      this.recipientBoxes.add(newbox);
      return this;
    }

    public Builder withPrivacyFlag(final int privacyFlag) {
      return this.withPrivacyMode(PrivacyMode.fromFlag(privacyFlag));
    }

    public Builder withPrivacyMode(final PrivacyMode privacyMode) {
      this.privacyMode = privacyMode;
      return this;
    }

    public Builder withAffectedContractTransactions(
        final Map<TxHash, byte[]> affectedContractTransactions) {
      this.affectedContractTransactions = affectedContractTransactions;
      return this;
    }

    public Builder withExecHash(final byte[] execHash) {
      this.execHash = execHash;
      return this;
    }

    public V2EncodedPayload build() {

      Map<TxHash, SecurityHash> affectedTxns =
          affectedContractTransactions.entrySet().stream()
              .collect(
                  Collectors.toUnmodifiableMap(
                      e -> e.getKey(), e -> SecurityHash.from(e.getValue())));

      List<RecipientBox> recipientBoxes =
          this.recipientBoxes.stream().map(RecipientBox::from).collect(Collectors.toList());

      return new V2EncodedPayload(
          senderKey,
          cipherText,
          cipherTextNonce,
          recipientBoxes,
          recipientNonce,
          recipientKeys,
          privacyMode,
          affectedTxns,
          execHash);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    V2EncodedPayload that = (V2EncodedPayload) o;
    return Objects.equals(senderKey, that.senderKey)
        && Arrays.equals(cipherText, that.cipherText)
        && Objects.equals(cipherTextNonce, that.cipherTextNonce)
        && Objects.equals(recipientBoxes, that.recipientBoxes)
        && Objects.equals(recipientNonce, that.recipientNonce)
        && Objects.equals(recipientKeys, that.recipientKeys)
        && privacyMode == that.privacyMode
        && Arrays.equals(execHash, that.execHash);
  }

  @Override
  public int hashCode() {
    int result =
        Objects.hash(
            senderKey, cipherTextNonce, recipientBoxes, recipientNonce, recipientKeys, privacyMode);
    result = 31 * result + Arrays.hashCode(cipherText);
    result = 31 * result + Arrays.hashCode(execHash);
    return result;
  }
}
