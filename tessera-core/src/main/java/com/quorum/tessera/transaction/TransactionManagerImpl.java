package com.quorum.tessera.transaction;

import com.quorum.tessera.data.*;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PublishPayloadException;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.transaction.exception.KeyNotFoundException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.util.Base64Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Delegate/Mediator object to normalise calls/interactions between Enclave and Base64Decoder
 *
 * @see {Base64Decoder}
 * @see {Enclave}
 */
public class TransactionManagerImpl implements TransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerImpl.class);

    private final PayloadEncoder payloadEncoder;

    private final Base64Codec base64Codec;

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final EncryptedRawTransactionDAO encryptedRawTransactionDAO;

    private final PartyInfoService partyInfoService;

    private final Enclave enclave;

    private final ResendManager resendManager;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    private int resendFetchSize;

    public TransactionManagerImpl(
            EncryptedTransactionDAO encryptedTransactionDAO,
            Enclave enclave,
            EncryptedRawTransactionDAO encryptedRawTransactionDAO,
            ResendManager resendManager,
            PartyInfoService partyInfoService,
            int resendFetchSize) {
        this(
                Base64Codec.create(),
                PayloadEncoder.create(),
                encryptedTransactionDAO,
                partyInfoService,
                enclave,
                encryptedRawTransactionDAO,
                resendManager,
                resendFetchSize);
    }

    /*
    Only use for tests
    */
    public TransactionManagerImpl(
            Base64Codec base64Codec,
            PayloadEncoder payloadEncoder,
            EncryptedTransactionDAO encryptedTransactionDAO,
            PartyInfoService partyInfoService,
            Enclave enclave,
            EncryptedRawTransactionDAO encryptedRawTransactionDAO,
            ResendManager resendManager,
            int resendFetchSize) {

        this.base64Codec = Objects.requireNonNull(base64Codec, "base64Decoder is required");
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder, "payloadEncoder is required");
        this.encryptedTransactionDAO =
                Objects.requireNonNull(encryptedTransactionDAO, "encryptedTransactionDAO is required");
        this.partyInfoService = Objects.requireNonNull(partyInfoService, "partyInfoService is required");
        this.enclave = Objects.requireNonNull(enclave, "enclave is required");
        this.encryptedRawTransactionDAO =
                Objects.requireNonNull(encryptedRawTransactionDAO, "encryptedRawTransactionDAO is required");
        this.resendManager = Objects.requireNonNull(resendManager, "resendManager is required");
        this.resendFetchSize = resendFetchSize;
    }

    @Override
    public SendResponse send(SendRequest sendRequest) {

        final PublicKey senderPublicKey = sendRequest.getSender();
        final List<PublicKey> recipientList = new ArrayList<>();
        recipientList.addAll(sendRequest.getRecipients());
        recipientList.add(senderPublicKey);
        recipientList.addAll(enclave.getForwardingKeys());

        final List<PublicKey> recipientListNoDuplicate = recipientList.stream().distinct().collect(Collectors.toList());

        final byte[] raw = sendRequest.getPayload();

        final EncodedPayload payload = enclave.encryptPayload(raw, senderPublicKey, recipientListNoDuplicate);

        final MessageHash transactionHash =
                Optional.of(payload)
                        .map(EncodedPayload::getCipherText)
                        .map(messageHashFactory::createFromCipherText)
                        .get();

        final EncryptedTransaction newTransaction =
                new EncryptedTransaction(transactionHash, this.payloadEncoder.encode(payload));

        this.encryptedTransactionDAO.save(newTransaction);

        recipientListNoDuplicate.forEach(
                recipient -> {
                    final EncodedPayload outgoing = payloadEncoder.forRecipient(payload, recipient);
                    partyInfoService.publishPayload(outgoing, recipient);
                });

        return SendResponse.from(transactionHash);
    }

    boolean publish(List<PublicKey> recipientList, EncodedPayload payload) {

        recipientList.stream()
                .filter(k -> !enclave.getPublicKeys().contains(k))
                .forEach(
                        recipient -> {
                            final EncodedPayload outgoing = payloadEncoder.forRecipient(payload, recipient);
                            partyInfoService.publishPayload(outgoing, recipient);
                        });
        return true;
    }

    @Override
    public SendResponse sendSignedTransaction(final SendSignedRequest sendRequest) {
        final List<PublicKey> recipientList = new ArrayList<>();
        recipientList.addAll(sendRequest.getRecipients());
        recipientList.addAll(enclave.getForwardingKeys());

        final MessageHash messageHash = new MessageHash(sendRequest.getSignedData());

        EncryptedRawTransaction encryptedRawTransaction =
                encryptedRawTransactionDAO
                        .retrieveByHash(messageHash)
                        .orElseThrow(
                                () ->
                                        new TransactionNotFoundException(
                                                "Raw Transaction with hash " + messageHash + " was not found"));

        recipientList.add(PublicKey.from(encryptedRawTransaction.getSender()));

        final List<PublicKey> recipientListNoDuplicate = recipientList.stream().distinct().collect(Collectors.toList());

        final EncodedPayload payload =
                enclave.encryptPayload(encryptedRawTransaction.toRawTransaction(), recipientListNoDuplicate);

        final EncryptedTransaction newTransaction =
                new EncryptedTransaction(messageHash, this.payloadEncoder.encode(payload));

        this.encryptedTransactionDAO.save(newTransaction);

        recipientListNoDuplicate.forEach(
                recipient -> {
                    final EncodedPayload toPublish = payloadEncoder.forRecipient(payload, recipient);
                    partyInfoService.publishPayload(toPublish, recipient);
                });

        return SendResponse.from(messageHash);
    }

    @Override
    public ResendResponse resend(ResendRequest request) {

        PublicKey recipientPublicKey = request.getRecipient();
        if (request.getType() == ResendRequestType.ALL) {

            int offset = 0;

            while (offset < encryptedTransactionDAO.transactionCount()) {

                encryptedTransactionDAO.retrieveTransactions(offset, resendFetchSize).stream()
                        .map(EncryptedTransaction::getEncodedPayload)
                        .map(payloadEncoder::decode)
                        .filter(
                                payload -> {
                                    final boolean isRecipient = payload.getRecipientKeys().contains(recipientPublicKey);
                                    final boolean isSender = Objects.equals(payload.getSenderKey(), recipientPublicKey);
                                    return isRecipient || isSender;
                                })
                        .forEach(
                                payload -> {
                                    final EncodedPayload prunedPayload;

                                    if (Objects.equals(payload.getSenderKey(), recipientPublicKey)) {
                                        final PublicKey decryptedKey =
                                                searchForRecipientKey(payload)
                                                        .orElseThrow(
                                                                () -> {
                                                                    final MessageHash hash =
                                                                            MessageHashFactory.create()
                                                                                    .createFromCipherText(
                                                                                            payload.getCipherText());
                                                                    return new KeyNotFoundException(
                                                                            "No key found as recipient of message "
                                                                                    + hash);
                                                                });
                                        payload.getRecipientKeys().add(decryptedKey);

                                        // This payload does not need to be pruned as it was not sent by this node and
                                        // so does not contain any other node's data
                                        prunedPayload = payload;
                                    } else {
                                        prunedPayload = payloadEncoder.forRecipient(payload, recipientPublicKey);
                                    }

                                    try {
                                        partyInfoService.publishPayload(prunedPayload, recipientPublicKey);
                                    } catch (PublishPayloadException ex) {
                                        LOGGER.warn(
                                                "Unable to resend payload to recipient with public key {}, due to {}",
                                                recipientPublicKey.encodeToBase64(),
                                                ex.getMessage());
                                    }
                                });

                offset += resendFetchSize;
            }

            return ResendResponse.Builder.create().build();
        } else {

            final MessageHash messageHash = request.getHash();

            final EncryptedTransaction encryptedTransaction =
                    encryptedTransactionDAO
                            .retrieveByHash(messageHash)
                            .orElseThrow(
                                    () ->
                                            new TransactionNotFoundException(
                                                    "Message with hash " + messageHash + " was not found"));

            final EncodedPayload payload = payloadEncoder.decode(encryptedTransaction.getEncodedPayload());

            final EncodedPayload returnValue;
            if (Objects.equals(payload.getSenderKey(), recipientPublicKey)) {
                final PublicKey decryptedKey = searchForRecipientKey(payload).orElseThrow(RuntimeException::new);
                payload.getRecipientKeys().add(decryptedKey);
                returnValue = payload;
            } else {
                // this is our tx
                returnValue = payloadEncoder.forRecipient(payload, recipientPublicKey);
            }

            return ResendResponse.Builder.create().withPayload(returnValue).build();
        }
    }

    @Override
    public MessageHash storePayload(final EncodedPayload payload) {

        final MessageHash transactionHash =
                Optional.of(payload)
                        .map(EncodedPayload::getCipherText)
                        .map(messageHashFactory::createFromCipherText)
                        .get();

        if (enclave.getPublicKeys().contains(payload.getSenderKey())) {
            this.resendManager.acceptOwnMessage(payload);
        } else {
            // this is a tx from someone else
            this.encryptedTransactionDAO.save(
                    new EncryptedTransaction(transactionHash, payloadEncoder.encode(payload)));
            LOGGER.info("Stored payload with hash {}", transactionHash);
        }

        return transactionHash;
    }

    @Override
    public void delete(MessageHash messageHash) {
        LOGGER.info("Received request to delete message with hash {}", messageHash);
        this.encryptedTransactionDAO.delete(messageHash);
    }

    @Override
    public ReceiveResponse receive(ReceiveRequest request) {

        final MessageHash hash = request.getTransactionHash();
        LOGGER.info("Lookup transaction {}", hash);

        final EncryptedTransaction encryptedTransaction =
                encryptedTransactionDAO
                        .retrieveByHash(hash)
                        .orElseThrow(
                                () -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));

        final EncodedPayload payload =
                Optional.of(encryptedTransaction)
                        .map(EncryptedTransaction::getEncodedPayload)
                        .map(payloadEncoder::decode)
                        .orElseThrow(() -> new IllegalStateException("Unable to decode previously encoded payload"));

        PublicKey recipientKey =
                request.getRecipient()
                        .orElse(
                                searchForRecipientKey(payload)
                                        .orElseThrow(
                                                () ->
                                                        new NoRecipientKeyFoundException(
                                                                "No suitable recipient keys found to decrypt payload for : "
                                                                        + hash)));

        byte[] response = enclave.unencryptTransaction(payload, recipientKey);
        return ReceiveResponse.from(response);
    }

    private Optional<PublicKey> searchForRecipientKey(final EncodedPayload payload) {
        for (final PublicKey potentialMatchingKey : enclave.getPublicKeys()) {
            try {
                enclave.unencryptTransaction(payload, potentialMatchingKey);
                return Optional.of(potentialMatchingKey);
            } catch (EnclaveException | IndexOutOfBoundsException | EncryptorException ex) {
                LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
            }
        }
        return Optional.empty();
    }

    @Override
    public StoreRawResponse store(StoreRawRequest storeRequest) {

        RawTransaction rawTransaction = enclave.encryptRawPayload(storeRequest.getPayload(), storeRequest.getSender());
        MessageHash hash = messageHashFactory.createFromCipherText(rawTransaction.getEncryptedPayload());

        EncryptedRawTransaction encryptedRawTransaction =
                new EncryptedRawTransaction(
                        hash,
                        rawTransaction.getEncryptedPayload(),
                        rawTransaction.getEncryptedKey(),
                        rawTransaction.getNonce().getNonceBytes(),
                        rawTransaction.getFrom().getKeyBytes());

        encryptedRawTransactionDAO.save(encryptedRawTransaction);

        return StoreRawResponse.from(encryptedRawTransaction.getHash());
    }

    @Override
    public boolean isSender(final MessageHash hash) {
        final EncodedPayload payload = this.fetchPayload(hash);
        return enclave.getPublicKeys().contains(payload.getSenderKey());
    }

    @Override
    public List<PublicKey> getParticipants(final MessageHash transactionHash) {
        final EncodedPayload payload = this.fetchPayload(transactionHash);

        // this includes the sender
        return payload.getRecipientKeys();
    }

    @Override
    public PublicKey defaultPublicKey() {
        return enclave.defaultPublicKey();
    }

    private EncodedPayload fetchPayload(final MessageHash hash) {
        return encryptedTransactionDAO
                .retrieveByHash(hash)
                .map(EncryptedTransaction::getEncodedPayload)
                .map(payloadEncoder::decode)
                .orElseThrow(
                        () ->
                                new TransactionNotFoundException(
                                        "Message with hash "
                                                + base64Codec.encodeToString(hash.getHashBytes())
                                                + " was not found"));
    }
}
