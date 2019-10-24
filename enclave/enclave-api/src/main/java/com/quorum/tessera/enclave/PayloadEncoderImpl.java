package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class PayloadEncoderImpl implements PayloadEncoder, BinaryEncoder {

    @Override
    public byte[] encode(final EncodedPayload payload) {

        final byte[] senderKey = encodeField(payload.getSenderKey().getKeyBytes());
        final byte[] cipherText = encodeField(payload.getCipherText());
        final byte[] nonce = encodeField(payload.getCipherTextNonce().getNonceBytes());
        final byte[] recipientNonce = encodeField(payload.getRecipientNonce().getNonceBytes());
        final byte[] recipients = encodeArray(payload.getRecipientBoxes());
        final byte[] recipientBytes =
                encodeArray(payload.getRecipientKeys().stream().map(PublicKey::getKeyBytes).collect(toList()));

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

        // this means there are no recipients in the payload (which we receive when we are a participant)
        if (!buffer.hasRemaining()) {
            return EncodedPayload.Builder.create()
                    .withSenderKey(PublicKey.from(senderKey))
                    .withCipherText(cipherText)
                    .withCipherTextNonce(nonce)
                    .withRecipientBoxes(recipientBoxes)
                    .withRecipientNonce(recipientNonce)
                    .withRecipientKeys(emptyList())
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

        return EncodedPayload.Builder.create()
                .withSenderKey(PublicKey.from(senderKey))
                .withCipherText(cipherText)
                .withCipherTextNonce(new Nonce(nonce))
                .withRecipientBoxes(recipientBoxes)
                .withRecipientNonce(new Nonce(recipientNonce))
                .withRecipientKeys(recipientKeys.stream().map(PublicKey::from).collect(toList()))
                .build();
    }

    @Override
    public EncodedPayload forRecipient(final EncodedPayload payload, final PublicKey recipient) {

        if (!payload.getRecipientKeys().contains(recipient)) {
            throw new InvalidRecipientException(
                    "Recipient " + recipient.encodeToBase64() + " is not a recipient of transaction ");
        }

        final int recipientIndex = payload.getRecipientKeys().indexOf(recipient);
        final byte[] recipientBox = payload.getRecipientBoxes().get(recipientIndex);

        return EncodedPayload.Builder.create()
                .withSenderKey(payload.getSenderKey())
                .withCipherText(payload.getCipherText())
                .withCipherTextNonce(payload.getCipherTextNonce())
                .withRecipientBoxes(singletonList(recipientBox))
                .withRecipientNonce(payload.getRecipientNonce())
                .withRecipientKeys(emptyList())
                .build();
    }
}
