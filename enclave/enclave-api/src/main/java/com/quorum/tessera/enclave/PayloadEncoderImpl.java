package com.quorum.tessera.enclave;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

import com.quorum.tessera.encryption.PublicKey;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class PayloadEncoderImpl implements PayloadEncoder, BinaryEncoder {

  @Override
  public byte[] encode(final EncodedPayload payload) {

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

    byte[] mandatoryRecipients = new byte[0];
    if (payload.getPrivacyMode() == PrivacyMode.MANDATORY_RECIPIENTS) {
      mandatoryRecipients =
          encodeArray(
              payload.getMandatoryRecipients().stream()
                  .map(PublicKey::getKeyBytes)
                  .collect(Collectors.toUnmodifiableList()));
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
                + mandatoryRecipients.length
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
        .put(mandatoryRecipients)
        .put(privacyGroupId)
        .array();
  }

  @Override
  public EncodedPayload decode(final byte[] input) {
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

    EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.create();

    payloadBuilder
        .withSenderKey(PublicKey.from(senderKey))
        .withCipherText(cipherText)
        .withCipherTextNonce(nonce)
        .withRecipientBoxes(recipientBoxes)
        .withRecipientNonce(recipientNonce);

    // this means there are no recipients in the payload (which we receive when we are a
    // participant)
    // TODO - not sure this is right
    if (!buffer.hasRemaining()) {
      return payloadBuilder
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

    payloadBuilder.withRecipientKeys(recipientKeys.stream().map(PublicKey::from).collect(toList()));

    if (!buffer.hasRemaining()) {
      return payloadBuilder
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

    payloadBuilder
        .withPrivacyMode(privacyMode)
        .withAffectedContractTransactions(affectedContractTransactions)
        .withExecHash(executionHash);

    if (buffer.hasRemaining()) {
      if (privacyMode == PrivacyMode.MANDATORY_RECIPIENTS) {
        final long mandatoryRecipientLength = buffer.getLong();

        final List<byte[]> mandatoryRecipients = new ArrayList<>();
        for (long i = 0; i < mandatoryRecipientLength; i++) {
          final long boxSize = buffer.getLong();
          final byte[] box = new byte[Math.toIntExact(boxSize)];
          buffer.get(box);
          mandatoryRecipients.add(box);
        }
        payloadBuilder.withMandatoryRecipients(
            mandatoryRecipients.stream().map(PublicKey::from).collect(Collectors.toSet()));
      }
    }

    if (!buffer.hasRemaining()) {
      return payloadBuilder.build();
    }

    final long privacyGroupIdSize = buffer.getLong();
    final byte[] privacyGroupId = new byte[Math.toIntExact(privacyGroupIdSize)];
    buffer.get(privacyGroupId);

    if (privacyGroupId.length > 0) {
      payloadBuilder.withPrivacyGroupId(PrivacyGroup.Id.fromBytes(privacyGroupId));
    }

    return payloadBuilder.build();
  }

  @Override
  public EncodedPayloadCodec encodedPayloadCodec() {
    return EncodedPayloadCodec.LEGACY;
  }
}
