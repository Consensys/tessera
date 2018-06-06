package com.github.nexus.transaction;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.encryption.Nonce;
import com.github.nexus.util.BinaryEncoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
        final byte[] senderKey = new byte[new Long(senderSize).intValue()];
        buffer.get(senderKey);

        final long cipherTextSize = buffer.getLong();
        final byte[] cipherText = new byte[new Long(cipherTextSize).intValue()];
        buffer.get(cipherText);

        final long nonceSize = buffer.getLong();
        final byte[] nonce = new byte[new Long(nonceSize).intValue()];
        buffer.get(nonce);

        final long numberOfRecipients = buffer.getLong();
        List<byte[]> recipientBoxes = new ArrayList<>();
        for(long i=0; i<numberOfRecipients; i++) {
            final long boxSize = buffer.getLong();
            final byte[] box = new byte[new Long(boxSize).intValue()];
            buffer.get(box);
            recipientBoxes.add(box);
        }

        final long recipientNonceSize = buffer.getLong();
        final byte[] recipientNonce = new byte[new Long(recipientNonceSize).intValue()];
        buffer.get(recipientNonce);

        return new EncodedPayload(
            new Key(senderKey),
            cipherText,
            new Nonce(nonce),
            recipientBoxes,
            new Nonce(recipientNonce)
        );
    }

}
