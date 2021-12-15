package com.quorum.tessera.enclave;

import static java.util.Collections.singletonList;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

/** This class contains the data that is sent to other nodes */
public class EncodedPayload {

  private final PublicKey senderKey;

  private final byte[] cipherText;

  private final Nonce cipherTextNonce;

  private final List<RecipientBox> recipientBoxes;

  private final Nonce recipientNonce;

  private final List<PublicKey> recipientKeys;

  private final PrivacyMode privacyMode;

  private final Map<TxHash, SecurityHash> affectedContractTransactions;

  private final byte[] execHash;

  private final PrivacyGroup.Id privacyGroupId;

  private final Set<PublicKey> mandatoryRecipients;

  private EncodedPayload(
      final PublicKey senderKey,
      final byte[] cipherText,
      final Nonce cipherTextNonce,
      final List<RecipientBox> recipientBoxes,
      final Nonce recipientNonce,
      final List<PublicKey> recipientKeys,
      final PrivacyMode privacyMode,
      final Map<TxHash, SecurityHash> affectedContractTransactions,
      final byte[] execHash,
      final PrivacyGroup.Id privacyGroupId,
      final Set<PublicKey> mandatoryRecipients) {
    this.senderKey = senderKey;
    this.cipherText = cipherText;
    this.cipherTextNonce = cipherTextNonce;
    this.recipientNonce = recipientNonce;
    this.recipientBoxes = recipientBoxes;
    this.recipientKeys = recipientKeys;
    this.privacyMode = privacyMode;
    this.affectedContractTransactions = affectedContractTransactions;
    this.execHash = execHash;
    this.privacyGroupId = privacyGroupId;
    this.mandatoryRecipients = mandatoryRecipients;
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

  public Optional<PrivacyGroup.Id> getPrivacyGroupId() {
    return Optional.ofNullable(privacyGroupId);
  }

  public Set<PublicKey> getMandatoryRecipients() {
    return mandatoryRecipients;
  }

  public static class Builder {

    private Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public static Builder from(EncodedPayload encodedPayload) {

      final Map<TxHash, byte[]> affectedContractTransactionMap =
          encodedPayload.getAffectedContractTransactions().entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getData()));

      final Builder builder =
          create()
              .withSenderKey(encodedPayload.getSenderKey())
              .withRecipientNonce(encodedPayload.getRecipientNonce())
              .withRecipientKeys(encodedPayload.getRecipientKeys())
              .withRecipientBoxes(
                  encodedPayload.getRecipientBoxes().stream()
                      .map(RecipientBox::getData)
                      .collect(Collectors.toList()))
              .withCipherText(encodedPayload.getCipherText())
              .withCipherTextNonce(encodedPayload.getCipherTextNonce())
              .withPrivacyMode(encodedPayload.getPrivacyMode())
              .withAffectedContractTransactions(affectedContractTransactionMap)
              .withMandatoryRecipients(encodedPayload.getMandatoryRecipients())
              .withExecHash(encodedPayload.getExecHash());

      encodedPayload.getPrivacyGroupId().ifPresent(builder::withPrivacyGroupId);

      return builder;
    }

    /**
     * Strips a payload of any data that isn't relevant to the given recipient Used to format a
     * payload before it is sent to the target node
     *
     * @param payload the full payload from which data needs to be stripped
     * @param recipient the recipient to retain information about
     * @return a payload which contains a subset of data from the input, which is relevant to the
     *     recipient
     */
    public static Builder forRecipient(final EncodedPayload payload, final PublicKey recipient) {

      final Builder builder = from(payload);

      if (!payload.getRecipientKeys().contains(recipient)) {
        throw new InvalidRecipientException(
            "Recipient " + recipient.encodeToBase64() + " is not a recipient of transaction ");
      }

      final int recipientIndex = payload.getRecipientKeys().indexOf(recipient);
      final byte[] recipientBox = payload.getRecipientBoxes().get(recipientIndex).getData();

      List<PublicKey> recipientList;

      if (PrivacyMode.PRIVATE_STATE_VALIDATION == payload.getPrivacyMode()) {
        recipientList = new ArrayList<>(payload.getRecipientKeys());
        recipientList.remove(recipientIndex);
        recipientList.add(0, recipient);
      } else {
        recipientList = singletonList(recipient);
      }

      builder.withRecipientBoxes(singletonList(recipientBox));
      builder.withNewRecipientKeys(recipientList);

      return builder;
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

    private PrivacyGroup.Id privacyGroupId;

    private Set<PublicKey> mandatoryRecipients = Collections.emptySet();

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

    public Builder withRecipientBox(byte[] newBox) {
      this.recipientBoxes.add(newBox);
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
      if (Objects.nonNull(execHash)) {
        this.execHash = execHash;
      }
      return this;
    }

    public Builder withPrivacyGroupId(final PrivacyGroup.Id privacyGroupId) {
      this.privacyGroupId = privacyGroupId;
      return this;
    }

    public Builder withMandatoryRecipients(final Set<PublicKey> mandatoryRecipients) {
      this.mandatoryRecipients = mandatoryRecipients;
      return this;
    }

    public EncodedPayload build() {

      Map<TxHash, SecurityHash> affectedTransactions =
          affectedContractTransactions.entrySet().stream()
              .collect(
                  Collectors.toUnmodifiableMap(
                      Map.Entry::getKey, e -> SecurityHash.from(e.getValue())));

      List<RecipientBox> recipientBoxes =
          this.recipientBoxes.stream().map(RecipientBox::from).collect(Collectors.toList());

      if ((privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) == (execHash.length == 0)) {
        throw new RuntimeException("ExecutionHash data is invalid");
      }

      if ((privacyMode == PrivacyMode.MANDATORY_RECIPIENTS) == mandatoryRecipients.isEmpty()) {
        throw new RuntimeException(
            "Mandatory recipients data only applicable for Mandatory Recipients privacy mode. "
                + "In case no mandatory recipient is required, consider using Party Protection privacy mode");
      }

      return new EncodedPayload(
          senderKey,
          cipherText,
          cipherTextNonce,
          recipientBoxes,
          recipientNonce,
          recipientKeys,
          privacyMode,
          affectedTransactions,
          execHash,
          privacyGroupId,
          mandatoryRecipients);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EncodedPayload that = (EncodedPayload) o;
    return Objects.equals(senderKey, that.senderKey)
        && Arrays.equals(cipherText, that.cipherText)
        && Objects.equals(cipherTextNonce, that.cipherTextNonce)
        && Objects.equals(recipientBoxes, that.recipientBoxes)
        && Objects.equals(recipientNonce, that.recipientNonce)
        && Objects.equals(recipientKeys, that.recipientKeys)
        && privacyMode == that.privacyMode
        && Arrays.equals(execHash, that.execHash)
        && Objects.equals(privacyGroupId, that.privacyGroupId)
        && Objects.equals(mandatoryRecipients, that.mandatoryRecipients);
  }

  @Override
  public int hashCode() {
    int result =
        Objects.hash(
            senderKey,
            cipherTextNonce,
            recipientBoxes,
            recipientNonce,
            recipientKeys,
            privacyMode,
            privacyGroupId,
            mandatoryRecipients);
    result = 31 * result + Arrays.hashCode(cipherText);
    result = 31 * result + Arrays.hashCode(execHash);
    return result;
  }
}
