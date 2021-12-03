package com.quorum.tessera.enclave.encoder;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class V3PayloadEncoder implements BinaryEncoder {

  public byte[] encode(final V3EncodedPayload payload) {

    final byte[] senderKey = encodeField(payload.getSenderKey().getKeyBytes());
    final byte[] cipherText = encodeField(payload.getCipherText());
    final byte[] nonce = encodeField(payload.getCipherTextNonce().getNonceBytes());
    final byte[] recipientNonce = encodeField(payload.getRecipientNonce().getNonceBytes());
    final byte[] recipients =
        encodeArray(
            payload.getRecipientBoxes().stream()
                .map(RecipientBox::getData)
                .collect(Collectors.toUnmodifiableList()));
    final byte[] recipientBytes =
        encodeArray(
            payload.getRecipientKeys().stream().map(PublicKey::getKeyBytes).collect(toList()));
    final PrivacyMode privacyMode =
        Optional.ofNullable(payload.getPrivacyMode()).orElse(PrivacyMode.STANDARD_PRIVATE);
    final byte[] privacyModeByte = encodeField(new byte[] {(byte) privacyMode.getPrivacyFlag()});

    final int affectedContractsPayloadLength =
        payload.getAffectedContractTransactions().entrySet().stream()
                .mapToInt(
                    entry -> entry.getKey().getBytes().length + entry.getValue().getData().length)
                .sum() // total size of all keys and values
            + Long.BYTES
            + // the number of entries in the map
            payload.getAffectedContractTransactions().size()
                * 2
                * Long.BYTES; // sizes of key and value lengths (for each entry)
    final ByteBuffer affectedContractTxs = ByteBuffer.allocate(affectedContractsPayloadLength);
    affectedContractTxs.putLong(payload.getAffectedContractTransactions().size());
    for (Map.Entry<TxHash, SecurityHash> entry :
        payload.getAffectedContractTransactions().entrySet()) {
      affectedContractTxs.putLong(entry.getKey().getBytes().length);
      affectedContractTxs.put(entry.getKey().getBytes());
      affectedContractTxs.putLong(entry.getValue().getData().length);
      affectedContractTxs.put(entry.getValue().getData());
    }
    byte[] executionHash = new byte[0];
    if (Objects.nonNull(payload.getExecHash()) && payload.getExecHash().length > 0) {
      executionHash = encodeField(payload.getExecHash());
    }

    byte[] privacyGroupId =
        payload
            .getPrivacyGroupId()
            .map(PrivacyGroup.Id::getBytes)
            .map(this::encodeField)
            .orElse(new byte[0]);

    return ByteBuffer.allocate(
            senderKey.length
                + cipherText.length
                + nonce.length
                + recipients.length
                + recipientNonce.length
                + recipientBytes.length
                + privacyModeByte.length
                + affectedContractsPayloadLength
                + executionHash.length
                + privacyGroupId.length)
        .put(senderKey)
        .put(cipherText)
        .put(nonce)
        .put(recipients)
        .put(recipientNonce)
        .put(recipientBytes)
        .put(privacyModeByte)
        .put(affectedContractTxs.array())
        .put(executionHash)
        .put(privacyGroupId)
        .array();
  }

  public V3EncodedPayload decode(final byte[] input) {
    final ByteBuffer buffer = ByteBuffer.wrap(input);

    final long senderSize = buffer.getLong();
    final byte[] senderKey = new byte[Math.toIntExact(senderSize)];
    buffer.get(senderKey);

    final long cipherTextSize = buffer.getLong();
    final byte[] cipherText = new byte[Math.toIntExact(cipherTextSize)];
    buffer.get(cipherText);

    final long nonceSize = buffer.getLong();
    final byte[] nonce = new byte[Math.toIntExact(nonceSize)];
    buffer.get(nonce);

    final long numberOfRecipients = buffer.getLong();
    final List<byte[]> recipientBoxes = new ArrayList<>();
    for (long i = 0; i < numberOfRecipients; i++) {
      final long boxSize = buffer.getLong();
      final byte[] box = new byte[Math.toIntExact(boxSize)];
      buffer.get(box);
      recipientBoxes.add(box);
    }

    final long recipientNonceSize = buffer.getLong();
    final byte[] recipientNonce = new byte[Math.toIntExact(recipientNonceSize)];
    buffer.get(recipientNonce);

    // this means there are no recipients in the payload (which we receive when we are a
    // participant)
    if (!buffer.hasRemaining()) {

      return V3EncodedPayload.Builder.create()
          .withSenderKey(PublicKey.from(senderKey))
          .withCipherText(cipherText)
          .withCipherTextNonce(nonce)
          .withRecipientBoxes(recipientBoxes)
          .withRecipientNonce(recipientNonce)
          .withRecipientKeys(emptyList())
          .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
          .withAffectedContractTransactions(emptyMap())
          .withExecHash(new byte[0])
          .build();
    }

    final long recipientLength = buffer.getLong();

    final List<byte[]> recipientKeys = new ArrayList<>();
    for (long i = 0; i < recipientLength; i++) {
      final long boxSize = buffer.getLong();
      final byte[] box = new byte[Math.toIntExact(boxSize)];
      buffer.get(box);
      recipientKeys.add(box);
    }

    if (!buffer.hasRemaining()) {

      return V3EncodedPayload.Builder.create()
          .withSenderKey(PublicKey.from(senderKey))
          .withCipherText(cipherText)
          .withCipherTextNonce(nonce)
          .withRecipientBoxes(recipientBoxes)
          .withRecipientNonce(recipientNonce)
          .withRecipientKeys(recipientKeys.stream().map(PublicKey::from).collect(toList()))
          .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
          .withAffectedContractTransactions(emptyMap())
          .withExecHash(new byte[0])
          .build();
    }

    final long privacyFlagLength = buffer.getLong();
    final byte[] privacyFlag = new byte[Math.toIntExact(privacyFlagLength)];
    buffer.get(privacyFlag);

    final long affectedContractTransactionsLength = buffer.getLong();
    final Map<TxHash, byte[]> affectedContractTransactions = new HashMap<>();
    for (long i = 0; i < affectedContractTransactionsLength; i++) {
      final long txHashSize = buffer.getLong();
      final byte[] txHash = new byte[Math.toIntExact(txHashSize)];
      buffer.get(txHash);

      final long txSecHashSize = buffer.getLong();
      final byte[] txSecHash = new byte[Math.toIntExact(txSecHashSize)];
      buffer.get(txSecHash);

      affectedContractTransactions.put(new TxHash(txHash), txSecHash);
    }

    final PrivacyMode privacyMode = PrivacyMode.fromFlag(privacyFlag[0]);

    byte[] executionHash = new byte[0];
    if (buffer.hasRemaining()) {
      if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
        final long executionHashSize = buffer.getLong();
        executionHash = new byte[Math.toIntExact(executionHashSize)];
        buffer.get(executionHash);
      }
    }

    if (!buffer.hasRemaining()) {
      return V3EncodedPayload.Builder.create()
          .withSenderKey(PublicKey.from(senderKey))
          .withCipherText(cipherText)
          .withCipherTextNonce(nonce)
          .withRecipientBoxes(recipientBoxes)
          .withRecipientNonce(recipientNonce)
          .withRecipientKeys(recipientKeys.stream().map(PublicKey::from).collect(toList()))
          .withPrivacyMode(privacyMode)
          .withAffectedContractTransactions(affectedContractTransactions)
          .withExecHash(executionHash)
          .build();
    }

    final long privacyGroupIdSize = buffer.getLong();
    final byte[] privacyGroupId = new byte[Math.toIntExact(privacyGroupIdSize)];
    buffer.get(privacyGroupId);

    return V3EncodedPayload.Builder.create()
        .withSenderKey(PublicKey.from(senderKey))
        .withCipherText(cipherText)
        .withCipherTextNonce(nonce)
        .withRecipientBoxes(recipientBoxes)
        .withRecipientNonce(recipientNonce)
        .withRecipientKeys(recipientKeys.stream().map(PublicKey::from).collect(toList()))
        .withPrivacyMode(privacyMode)
        .withAffectedContractTransactions(affectedContractTransactions)
        .withExecHash(executionHash)
        .withPrivacyGroupId(PrivacyGroup.Id.fromBytes(privacyGroupId))
        .build();
  }

  public V3EncodedPayload forRecipient(final V3EncodedPayload payload, final PublicKey recipient) {

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

    Map<TxHash, byte[]> affectedTxnMap =
        payload.getAffectedContractTransactions().entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getData()));

    final V3EncodedPayload.Builder builder =
        V3EncodedPayload.Builder.create()
            .withSenderKey(payload.getSenderKey())
            .withCipherText(payload.getCipherText())
            .withCipherTextNonce(payload.getCipherTextNonce())
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(payload.getRecipientNonce())
            .withRecipientKeys(recipientList)
            .withPrivacyMode(payload.getPrivacyMode())
            .withAffectedContractTransactions(affectedTxnMap)
            .withExecHash(payload.getExecHash());
    payload.getPrivacyGroupId().ifPresent(builder::withPrivacyGroupId);

    return builder.build();
  }

  public V3EncodedPayload withRecipient(final V3EncodedPayload payload, final PublicKey recipient) {
    // this method is to be used for adding a recipient to an EncodedPayload that does not have any.
    if (!payload.getRecipientKeys().isEmpty()) {
      return payload;
    }
    return V3EncodedPayload.Builder.from(payload).withRecipientKey(recipient).build();
  }
}
