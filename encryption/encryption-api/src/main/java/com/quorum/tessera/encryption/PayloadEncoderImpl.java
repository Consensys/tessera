package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.Nonce;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class PayloadEncoderImpl implements PayloadEncoder, BinaryEncoder {

    @Override
    public byte[] encode(final EncodedPayloadWithRecipients outer) {
        final EncodedPayload inner = outer.getEncodedPayload();

        final byte[] senderKey = encodeField(inner.getSenderKey().getKeyBytes());
        final byte[] cipherText = encodeField(inner.getCipherText());
        final byte[] nonce = encodeField(inner.getCipherTextNonce().getNonceBytes());
        final byte[] recipientNonce = encodeField(inner.getRecipientNonce().getNonceBytes());
        final byte[] recipients = encodeArray(inner.getRecipientBoxes());
        final byte[] recipientBytes
            = encodeArray(outer.getRecipientKeys().stream().map(PublicKey::getKeyBytes).collect(toList()));

        return ByteBuffer
            .allocate(senderKey.length + cipherText.length + nonce.length + recipients.length + recipientNonce.length + recipientBytes.length)
            .put(senderKey)
            .put(cipherText)
            .put(nonce)
            .put(recipients)
            .put(recipientNonce)
            .put(recipientBytes)
            .array();
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
            PublicKey.from(senderKey),
            cipherText,
            new Nonce(nonce),
            recipientBoxes,
            new Nonce(recipientNonce)
        );

        //this means there are no recipients in the payload (which we receive when we are a participant)
        if (!buffer.hasRemaining()) {
            return new EncodedPayloadWithRecipients(payload, emptyList());
        }

        final long recipientLength = buffer.getLong();

        final List<byte[]> recipientKeys = new ArrayList<>();
        for (long i = 0; i < recipientLength; i++) {
            final long boxSize = buffer.getLong();
            final byte[] box = new byte[Math.toIntExact(boxSize)];
            buffer.get(box);
            recipientKeys.add(box);
        }

        return new EncodedPayloadWithRecipients(payload, recipientKeys.stream().map(PublicKey::from).collect(toList()));
    }

    @Override
    public EncodedPayloadWithRecipients forRecipient(final EncodedPayloadWithRecipients input,
                                                     final PublicKey recipient) {
        final EncodedPayload encodedPayload = input.getEncodedPayload();

        if (!input.getRecipientKeys().contains(recipient)) {
            throw new InvalidRecipientException("Recipient " + recipient.encodeToBase64() + " is not a recipient of transaction ");
        }

        final int recipientIndex = input.getRecipientKeys().indexOf(recipient);
        final byte[] recipientBox = encodedPayload.getRecipientBoxes().get(recipientIndex);

        return new EncodedPayloadWithRecipients(
            new EncodedPayload(
                encodedPayload.getSenderKey(),
                encodedPayload.getCipherText(),
                encodedPayload.getCipherTextNonce(),
                singletonList(recipientBox),
                encodedPayload.getRecipientNonce()
            ),
            emptyList()
        );
    }

}
