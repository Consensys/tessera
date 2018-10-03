package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.util.Base64Decoder;
import java.util.Objects;
import java.util.Optional;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate/Mediator object to normalise calls/interactions between Enclave and
 * Base64Decoder
 *
 * @see {Base64Decoder}
 * @see {Enclave}
 */
public class TransactionManagerImpl implements TransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerImpl.class);

    private final Enclave enclave;

    private final PayloadEncoder payloadEncoder;

    private final Base64Decoder base64Decoder;

    private final TransactionService transactionService;
    
    public TransactionManagerImpl(Enclave enclave, Base64Decoder base64Decoder, PayloadEncoder payloadEncoder,TransactionService transactionService) {
        this.enclave = Objects.requireNonNull(enclave);
        this.base64Decoder = Objects.requireNonNull(base64Decoder);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.transactionService = Objects.requireNonNull(transactionService);
    }

    @Override
    public SendResponse send(SendRequest sendRequest) {

        LOGGER.debug("Received send request");

        final String sender = sendRequest.getFrom();
        LOGGER.debug("Received send request from {}", sender);
        final Optional<byte[]> from = Optional.ofNullable(sender)
                .map(base64Decoder::decode);

        LOGGER.debug("SEND: sender {}", sender);

        final byte[][] recipients = Stream
                .of(sendRequest.getTo())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        LOGGER.debug("SEND: recipients {}", Stream.of(sendRequest.getTo()).collect(joining()));

        final byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        final byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);
        return new SendResponse(encodedKey);
    }

    @Override
    public String storeAndEncodeKey(String sender, String recipientKeys, byte[] payload) {
        final Optional<byte[]> from = Optional
                .ofNullable(sender)
                .map(base64Decoder::decode);

        final String nonnullRecipients = Optional.ofNullable(recipientKeys).orElse("");
        final byte[][] recipients = Stream.of(nonnullRecipients.split(","))
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        LOGGER.debug("SendRaw Recipients: {}", nonnullRecipients);

        final byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);

        LOGGER.debug("Encoded key: {}", encodedKey);

        return encodedKey;
    }

    @Override
    public String receiveAndEncode(ReceiveRequest request) {

        final byte[] key = base64Decoder.decode(request.getKey());

        final Optional<byte[]> to = Optional
                .ofNullable(request.getTo())
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(key, to);

        return base64Decoder.encodeToString(payload);
    }

    @Override
    public Optional<byte[]> resendAndEncode(ResendRequest request) {

        final byte[] publicKey = base64Decoder.decode(request.getPublicKey());

        if (request.getType() == ResendRequestType.ALL) {
            enclave.resendAll(publicKey);
            return Optional.empty();
        } else {
            final byte[] hashKey = base64Decoder.decode(request.getKey());

            final EncodedPayloadWithRecipients payloadWithRecipients = enclave
                    .fetchTransactionForRecipient(new MessageHash(hashKey), new Key(publicKey));

            final byte[] encoded = payloadEncoder.encode(payloadWithRecipients);

            return Optional.of(encoded);
        }
    }

    @Override
    public void storePayload(byte[] payload) {
        final MessageHash messageHash = enclave.storePayload(payload);
        LOGGER.info(base64Decoder.encodeToString(messageHash.getHashBytes()));
    }

    @Override
    public void delete(DeleteRequest request) {
        final byte[] hashBytes = base64Decoder.decode(request.getKey());
        enclave.delete(hashBytes);
    }

    @Override
    public ReceiveResponse receive(String hash, String toStr) {

        final byte[] key = base64Decoder.decode(hash);

        final Optional<byte[]> to = Optional
                .ofNullable(toStr)
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(key, to);

        final String encodedPayload = base64Decoder.encodeToString(payload);

        return new ReceiveResponse(encodedPayload);
    }

    @Override
    public byte[] receiveRaw(String hash, String recipientKey) {
        
        final byte[] decodedKey = base64Decoder.decode(hash);

        final Optional<byte[]> to = Optional
                .ofNullable(recipientKey)
                .map(base64Decoder::decode);

        return enclave.receive(decodedKey, to);
    }

    @Override
    public void deleteKey(String key) {
        final byte[] hashBytes = base64Decoder.decode(key);
        enclave.delete(hashBytes);
    }

    public ReceiveResponse receive(ReceiveRequest request) {
        return receive(request.getKey(),request.getTo());
    }



}
