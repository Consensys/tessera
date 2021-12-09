package com.quorum.tessera.enclave;

import static java.util.Collections.singletonList;

import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

/** Encodes and decodes a {@link EncodedPayload} to and from its binary representation */
public interface PayloadEncoder {

  /**
   * Encodes the payload to a byte array
   *
   * @param payload the payload to encode
   * @return the byte array representing the encoded payload
   */
  byte[] encode(EncodedPayload payload);

  /**
   * Decodes a byte array back into an encrypted payload
   *
   * @param input The byte array to decode into an EncodedPayload
   * @return the decoded payload
   */
  EncodedPayload decode(byte[] input);

  /**
   * Strips a payload of any data that isn't relevant to the given recipient Used to format a
   * payload before it is sent to the target node
   *
   * @param payload the full payload from which data needs to be stripped
   * @param recipient the recipient to retain information about
   * @return a payload which contains a subset of data from the input, which is relevant to the
   *     recipient
   */
  default EncodedPayload forRecipient(final EncodedPayload payload, final PublicKey recipient) {

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

    final EncodedPayload.Builder builder =
        EncodedPayload.Builder.create()
            .withSenderKey(payload.getSenderKey())
            .withCipherText(payload.getCipherText())
            .withCipherTextNonce(payload.getCipherTextNonce())
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(payload.getRecipientNonce())
            .withRecipientKeys(recipientList)
            .withPrivacyMode(payload.getPrivacyMode())
            .withAffectedContractTransactions(affectedTxnMap)
            .withExecHash(payload.getExecHash())
            .withMandatoryRecipients(payload.getMandatoryRecipients());

    payload.getPrivacyGroupId().ifPresent(builder::withPrivacyGroupId);

    return builder.build();
  }

  EncodedPayloadCodec encodedPayloadCodec();

  static PayloadEncoder create(EncodedPayloadCodec encodedPayloadCodec) {
    return ServiceLoader.load(PayloadEncoder.class).stream()
        .map(ServiceLoader.Provider::get)
        .filter(e -> e.encodedPayloadCodec() == encodedPayloadCodec)
        .reduce(
            (l, r) -> {
              throw new IllegalStateException(
                  "Resolved multiple encoders for codec " + encodedPayloadCodec);
            })
        .orElseThrow(
            () -> new IllegalStateException("No encoder found for " + encodedPayloadCodec));
  }
}
