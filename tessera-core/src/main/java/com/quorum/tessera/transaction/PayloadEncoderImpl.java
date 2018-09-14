package com.quorum.tessera.transaction;

import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.util.BinaryEncoder;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.Nonce;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PayloadEncoderImpl implements PayloadEncoder, BinaryEncoder {

    @Override
    public byte[] encode(final EncodedPayload encodedPayload) {
        final byte[] senderKey = encodeField(encodedPayload.getSenderKey().getKeyBytes());
        final byte[] cipherText = encodeField(encodedPayload.getCipherText());
        final byte[] nonce = encodeField(encodedPayload.getCipherTextNonce().getNonceBytes());
        final byte[] recipientNonce = encodeField(encodedPayload.getRecipientNonce().getNonceBytes());
        final byte[] recipients = encodeArray(encodedPayload.getRecipientBoxes());

        return ByteBuffer.allocate(senderKey.length + cipherText.length + nonce.length + recipients.length + recipientNonce.length)
                .put(senderKey)
                .put(cipherText)
                .put(nonce)
                .put(recipients)
                .put(recipientNonce)
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

        return new EncodedPayload(
                new Key(senderKey),
                cipherText,
                new Nonce(nonce),
                recipientBoxes,
                new Nonce(recipientNonce)
        );
    }

    @Override
    public byte[] encode(final EncodedPayloadWithRecipients encodedPayloadWithRecipients) {
        final byte[] payloadBytes = encode(encodedPayloadWithRecipients.getEncodedPayload());

        final List<byte[]> keysAsBytes = encodedPayloadWithRecipients
                .getRecipientKeys()
                .stream()
                .map(Key::getKeyBytes)
                .collect(toList());

        final byte[] recipientBytes = encodeArray(keysAsBytes);

        final byte[] returnValue = new byte[payloadBytes.length + recipientBytes.length];

        System.arraycopy(payloadBytes, 0, returnValue, 0, payloadBytes.length);
        System.arraycopy(recipientBytes, 0, returnValue, payloadBytes.length, recipientBytes.length);

        return returnValue;
    }

    @Override
    public EncodedPayloadWithRecipients decodePayloadWithRecipients(final byte[] input) {
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

        EncodedPayload payload = new EncodedPayload(
                new Key(senderKey),
                cipherText,
                new Nonce(nonce),
                recipientBoxes,
                new Nonce(recipientNonce)
        );

        final long recipientLength = buffer.getLong();

        final List<byte[]> recipientKeys = new ArrayList<>();
        for (long i = 0; i < recipientLength; i++) {
            final long boxSize = buffer.getLong();
            final byte[] box = new byte[Math.toIntExact(boxSize)];
            buffer.get(box);
            recipientKeys.add(box);
        }

        return new EncodedPayloadWithRecipients(
                payload,
                recipientKeys.stream().map(Key::new).collect(toList())
        );
    }

}
