package com.quorum.tessera.enclave.encoder;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.quorum.tessera.enclave.BinaryEncoder;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Do NOT change as this is a copy of the legacy payload encoder. The tests will use these logic to
 * ensure the legacy encoder is still able to understand new encoded payload and vice versa
 */
public class LegacyPayloadEncoder implements BinaryEncoder {

  public byte[] encode(final LegacyEncodedPayload payload) {

    final byte[] senderKey = encodeField(payload.getSenderKey().getKeyBytes());
    final byte[] cipherText = encodeField(payload.getCipherText());
    final byte[] nonce = encodeField(payload.getCipherTextNonce().getNonceBytes());
    final byte[] recipientNonce = encodeField(payload.getRecipientNonce().getNonceBytes());
    final byte[] recipients = encodeArray(payload.getRecipientBoxes());
    final byte[] recipientBytes =
        encodeArray(
            payload.getRecipientKeys().stream().map(PublicKey::getKeyBytes).collect(toList()));

    return ByteBuffer.allocate(
            senderKey.length
                + cipherText.length
                + nonce.length
                + recipients.length
                + recipientNonce.length
                + recipientBytes.length)
        .put(senderKey)
        .put(cipherText)
        .put(nonce)
        .put(recipients)
        .put(recipientNonce)
        .put(recipientBytes)
        .array();
  }

  public LegacyEncodedPayload decode(final byte[] input) {
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
      return new LegacyEncodedPayload(
          PublicKey.from(senderKey),
          cipherText,
          new Nonce(nonce),
          recipientBoxes,
          new Nonce(recipientNonce),
          emptyList());
    }

    final long recipientLength = buffer.getLong();

    final List<byte[]> recipientKeys = new ArrayList<>();
    for (long i = 0; i < recipientLength; i++) {
      final long boxSize = buffer.getLong();
      final byte[] box = new byte[Math.toIntExact(boxSize)];
      buffer.get(box);
      recipientKeys.add(box);
    }

    return new LegacyEncodedPayload(
        PublicKey.from(senderKey),
        cipherText,
        new Nonce(nonce),
        recipientBoxes,
        new Nonce(recipientNonce),
        recipientKeys.stream().map(PublicKey::from).collect(toList()));
  }
}
