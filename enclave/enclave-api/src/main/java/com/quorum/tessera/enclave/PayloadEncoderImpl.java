package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;

import java.nio.ByteBuffer;
import java.util.*;

import static java.util.Collections.*;
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
        final PrivacyMode privacyMode = Optional.of(payload.getPrivacyMode()).orElse(PrivacyMode.STANDARD_PRIVATE);
        final byte[] privacyModeByte = encodeField(new byte[] {(byte) privacyMode.getPrivacyFlag()});

        final int affectedContractsPayloadLength =
                payload.getAffectedContractTransactions().entrySet().stream()
                                .mapToInt(entry -> entry.getKey().getBytes().length + entry.getValue().length)
                                .sum() // total size of all keys and values
                        + Long.BYTES
                        + // the number of entries in the map
                        payload.getAffectedContractTransactions().size()
                                * 2
                                * Long.BYTES; // sizes of key and value lengths (for each entry)
        final ByteBuffer affectedContractTxs = ByteBuffer.allocate(affectedContractsPayloadLength);
        affectedContractTxs.putLong(payload.getAffectedContractTransactions().size());
        for (Map.Entry<TxHash, byte[]> entry : payload.getAffectedContractTransactions().entrySet()) {
            affectedContractTxs.putLong(entry.getKey().getBytes().length);
            affectedContractTxs.put(entry.getKey().getBytes());
            affectedContractTxs.putLong(entry.getValue().length);
            affectedContractTxs.put(entry.getValue());
        }
        byte[] executionHash = new byte[0];
        if (Objects.nonNull(payload.getExecHash()) && payload.getExecHash().length > 0) {
            executionHash = encodeField(payload.getExecHash());
        }

        return ByteBuffer.allocate(
                        senderKey.length
                                + cipherText.length
                                + nonce.length
                                + recipients.length
                                + recipientNonce.length
                                + recipientBytes.length
                                + privacyModeByte.length
                                + affectedContractsPayloadLength
                                + executionHash.length)
                .put(senderKey)
                .put(cipherText)
                .put(nonce)
                .put(recipients)
                .put(recipientNonce)
                .put(recipientBytes)
                .put(privacyModeByte)
                .put(affectedContractTxs.array())
                .put(executionHash)
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
        // TODO - not sure this is right
        if (!buffer.hasRemaining()) {
            return new EncodedPayload(
                    PublicKey.from(senderKey),
                    cipherText,
                    new Nonce(nonce),
                    recipientBoxes,
                    new Nonce(recipientNonce),
                    emptyList(),
                    PrivacyMode.STANDARD_PRIVATE,
                    emptyMap(),
                    new byte[0]);
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
            return new EncodedPayload(
                    PublicKey.from(senderKey),
                    cipherText,
                    new Nonce(nonce),
                    recipientBoxes,
                    new Nonce(recipientNonce),
                    recipientKeys.stream().map(PublicKey::from).collect(toList()),
                    PrivacyMode.STANDARD_PRIVATE,
                    emptyMap(),
                    new byte[0]);
        }

        final long privacyModeLength = buffer.getLong();
        final byte[] privacyMode = new byte[Math.toIntExact(privacyModeLength)];
        buffer.get(privacyMode);

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

        byte[] executionHash = new byte[0];

        if (buffer.hasRemaining()) {
            final long executionHashSize = buffer.getLong();
            executionHash = new byte[Math.toIntExact(executionHashSize)];
            buffer.get(executionHash);
        }

        return new EncodedPayload(
                PublicKey.from(senderKey),
                cipherText,
                new Nonce(nonce),
                recipientBoxes,
                new Nonce(recipientNonce),
                recipientKeys.stream().map(PublicKey::from).collect(toList()),
                PrivacyMode.fromFlag(privacyMode[0]),
                affectedContractTransactions,
                executionHash);
    }

    @Override
    public EncodedPayload forRecipient(final EncodedPayload payload, final PublicKey recipient) {

        if (!payload.getRecipientKeys().contains(recipient)) {
            throw new InvalidRecipientException(
                    "Recipient " + recipient.encodeToBase64() + " is not a recipient of transaction ");
        }

        final int recipientIndex = payload.getRecipientKeys().indexOf(recipient);
        final byte[] recipientBox = payload.getRecipientBoxes().get(recipientIndex);

        List<PublicKey> recipientList;

        if (PrivacyMode.PRIVATE_STATE_VALIDATION == payload.getPrivacyMode()) {
            recipientList = new ArrayList<>(payload.getRecipientKeys());
            recipientList.remove(recipientIndex);
            recipientList.add(0, recipient);
        } else {
            recipientList = singletonList(recipient);
        }

        EncodedPayload result =
                new EncodedPayload(
                        payload.getSenderKey(),
                        payload.getCipherText(),
                        payload.getCipherTextNonce(),
                        singletonList(recipientBox),
                        payload.getRecipientNonce(),
                        recipientList,
                        payload.getPrivacyMode(),
                        payload.getAffectedContractTransactions(),
                        payload.getExecHash());

        return result;
    }

    @Override
    public EncodedPayload withRecipient(final EncodedPayload payload, final PublicKey recipient) {
        // this method is to be used for adding a recipient to an EncodedPayload that does not have any.
        if (!payload.getRecipientKeys().isEmpty()) {
            return payload;
        }
        try {
            // if the encoded payload was send by constellation the recipientKeys is an EMPTY_LIST
            payload.getRecipientKeys().add(recipient);
            return payload;
        } catch (UnsupportedOperationException e) {
            return new EncodedPayload(
                    payload.getSenderKey(),
                    payload.getCipherText(),
                    payload.getCipherTextNonce(),
                    payload.getRecipientBoxes(),
                    payload.getRecipientNonce(),
                    Arrays.asList(recipient),
                    payload.getPrivacyMode(),
                    payload.getAffectedContractTransactions(),
                    payload.getExecHash());
        }
    }
}
