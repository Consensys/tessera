package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.partyinfo.ResendResponse;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.api.model.*;
import com.quorum.tessera.data.EncryptedRawTransaction;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PublishPayloadException;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.util.Base64Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final PrivacyHelper privacyHelper;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    private int resendFetchSize;

    public TransactionManagerImpl(
            EncryptedTransactionDAO encryptedTransactionDAO,
            Enclave enclave,
            EncryptedRawTransactionDAO encryptedRawTransactionDAO,
            ResendManager resendManager,
            PartyInfoService partyInfoService,
            PrivacyHelper privacyHelper,
            int resendFetchSize) {
        this(
                Base64Codec.create(),
                PayloadEncoder.create(),
                encryptedTransactionDAO,
                partyInfoService,
                enclave,
                encryptedRawTransactionDAO,
                resendManager,
                privacyHelper,
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
            PrivacyHelper privacyHelper,
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
        this.privacyHelper = Objects.requireNonNull(privacyHelper, "privacyManager is required");
        this.resendFetchSize = resendFetchSize;
    }

    @Override
    public SendResponse send(SendRequest sendRequest) {

        final String sender = sendRequest.getFrom();

        final PublicKey senderPublicKey =
                Optional.ofNullable(sender)
                        .map(base64Codec::decode)
                        .map(PublicKey::from)
                        .orElseGet(enclave::defaultPublicKey);

        final byte[][] recipients =
                Stream.of(sendRequest)
                        .filter(sr -> Objects.nonNull(sr.getTo()))
                        .flatMap(s -> Stream.of(s.getTo()))
                        .map(base64Codec::decode)
                        .toArray(byte[][]::new);

        final List<PublicKey> recipientList = Stream.of(recipients).map(PublicKey::from).collect(Collectors.toList());

        recipientList.add(senderPublicKey);

        recipientList.addAll(enclave.getForwardingKeys());

        final List<PublicKey> recipientListNoDuplicate = recipientList.stream().distinct().collect(Collectors.toList());

        final byte[] raw = sendRequest.getPayload();

        final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendRequest.getPrivacyFlag());

        final byte[] execHash =
                Optional.ofNullable(sendRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

        final List<AffectedTransaction> affectedContractTransactions =
                privacyHelper.findAffectedContractTransactionsFromSendRequest(
                        sendRequest.getAffectedContractTransactions());

        privacyHelper.validateSendRequest(privacyMode, recipientList, affectedContractTransactions);

        final EncodedPayload payload =
                enclave.encryptPayload(
                        raw,
                        senderPublicKey,
                        recipientListNoDuplicate,
                        privacyMode,
                        affectedContractTransactions,
                        execHash);

        final MessageHash transactionHash =
                Optional.of(payload)
                        .map(EncodedPayload::getCipherText)
                        .map(messageHashFactory::createFromCipherText)
                        .get();

        final EncryptedTransaction newTransaction =
                new EncryptedTransaction(transactionHash, this.payloadEncoder.encode(payload));

        this.encryptedTransactionDAO.save(newTransaction);

        publish(recipientListNoDuplicate, payload);

        final byte[] key = transactionHash.getHashBytes();

        final String encodedKey = base64Codec.encodeToString(key);

        return new SendResponse(encodedKey);
    }

    void publish(List<PublicKey> recipientList, EncodedPayload payload) {
        recipientList.stream()
                .filter(k -> !enclave.getPublicKeys().contains(k))
                .forEach(
                        recipient -> {
                            final EncodedPayload outgoing = payloadEncoder.forRecipient(payload, recipient);
                            partyInfoService.publishPayload(outgoing, recipient);
                        });
    }

    @Override
    public SendResponse sendSignedTransaction(final SendSignedRequest sendRequest) {

        final byte[][] recipients =
                Stream.of(sendRequest)
                        .filter(sr -> Objects.nonNull(sr.getTo()))
                        .flatMap(s -> Stream.of(s.getTo()))
                        .map(base64Codec::decode)
                        .toArray(byte[][]::new);

        final List<PublicKey> recipientList = Stream.of(recipients).map(PublicKey::from).collect(Collectors.toList());

        recipientList.addAll(enclave.getForwardingKeys());

        final MessageHash messageHash = new MessageHash(sendRequest.getHash());

        EncryptedRawTransaction encryptedRawTransaction =
                encryptedRawTransactionDAO
                        .retrieveByHash(messageHash)
                        .orElseThrow(
                                () ->
                                        new TransactionNotFoundException(
                                                "Raw Transaction with hash " + messageHash + " was not found"));

        recipientList.add(PublicKey.from(encryptedRawTransaction.getSender()));

        final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendRequest.getPrivacyFlag());

        final byte[] execHash =
                Optional.ofNullable(sendRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

        final List<AffectedTransaction> affectedContractTransactions =
                privacyHelper.findAffectedContractTransactionsFromSendRequest(
                        sendRequest.getAffectedContractTransactions());

        privacyHelper.validateSendRequest(privacyMode, recipientList, affectedContractTransactions);

        final List<PublicKey> recipientListNoDuplicate = recipientList.stream().distinct().collect(Collectors.toList());

        final EncodedPayload payload =
                enclave.encryptPayload(
                        encryptedRawTransaction.toRawTransaction(),
                        recipientListNoDuplicate,
                        privacyMode,
                        affectedContractTransactions,
                        execHash);

        final EncryptedTransaction newTransaction =
                new EncryptedTransaction(messageHash, this.payloadEncoder.encode(payload));

        this.encryptedTransactionDAO.save(newTransaction);

        publish(recipientListNoDuplicate, payload);

        final byte[] key = messageHash.getHashBytes();

        final String encodedKey = base64Codec.encodeToString(key);

        return new SendResponse(encodedKey);
    }

    @Override
    public ResendResponse resend(ResendRequest request) {

        final byte[] publicKeyData = base64Codec.decode(request.getPublicKey());
        PublicKey recipientPublicKey = PublicKey.from(publicKeyData);
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
                                        if (payload.getRecipientKeys().isEmpty()) {
                                            // TODO Should we stop the whole resend just because we could not find a key
                                            // for a tx? Log instead?
                                            // a malicious party may be able to craft TXs that prevent others from
                                            // performing resends
                                            final PublicKey decryptedKey =
                                                    searchForRecipientKey(payload)
                                                            .orElseThrow(
                                                                    () -> {
                                                                        final MessageHash hash =
                                                                                MessageHashFactory.create()
                                                                                        .createFromCipherText(
                                                                                                payload
                                                                                                        .getCipherText());
                                                                        return new RecipientKeyNotFoundException(
                                                                                "No key found as recipient of message "
                                                                                        + hash);
                                                                    });

                                            prunedPayload = payloadEncoder.withRecipient(payload, decryptedKey);
                                        } else {
                                            prunedPayload = payload;
                                        }
                                    } else {
                                        prunedPayload = payloadEncoder.forRecipient(payload, recipientPublicKey);
                                    }

                                    try {
                                        if (!enclave.getPublicKeys().contains(recipientPublicKey)) {
                                            partyInfoService.publishPayload(prunedPayload, recipientPublicKey);
                                        }
                                    } catch (PublishPayloadException ex) {
                                        LOGGER.warn(
                                                "Unable to publish payload to recipient {} during resend",
                                                recipientPublicKey.encodeToBase64());
                                    }
                                });

                offset += resendFetchSize;
            }

            return new ResendResponse();
        } else {

            final byte[] hashKey = base64Codec.decode(request.getKey());
            final MessageHash messageHash = new MessageHash(hashKey);

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
                returnValue = EncodedPayload.Builder.from(payload)
                    .withRecipientKey(decryptedKey)
                    .build();
            } else {
                returnValue = payloadEncoder.forRecipient(payload, recipientPublicKey);
            }
            return new ResendResponse(payloadEncoder.encode(returnValue));
        }
    }

    @Override
    public MessageHash storePayload(byte[] input) {

        final EncodedPayload payload = payloadEncoder.decode(input);

        final MessageHash transactionHash =
                Optional.of(payload)
                        .map(EncodedPayload::getCipherText)
                        .map(messageHashFactory::createFromCipherText)
                        .get();

        final List<AffectedTransaction> affectedContractTransactions =
                privacyHelper.findAffectedContractTransactionsFromPayload(payload);

        if (!privacyHelper.validatePayload(
                TxHash.from(transactionHash.getHashBytes()), payload, affectedContractTransactions)) {
            return transactionHash;
        }

        final Set<TxHash> invalidSecurityHashes =
                enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);

        byte[] sanitizedInput = input;
        if (!invalidSecurityHashes.isEmpty()) {
            final EncodedPayload updatedPayload =
                    privacyHelper.sanitisePrivacyPayload(
                            TxHash.from(transactionHash.getHashBytes()), payload, invalidSecurityHashes);
            sanitizedInput = payloadEncoder.encode(updatedPayload);
        }
        // TODO - remove extra logs
        LOGGER.info(
                "AffectedContractTransaction.size={} InvalidSecurityHashes.size={}",
                affectedContractTransactions.size(),
                invalidSecurityHashes.size());

        if (enclave.getPublicKeys().contains(payload.getSenderKey())) {

            this.resendManager.acceptOwnMessage(sanitizedInput);

        } else {

            // this is a tx from someone else
            this.encryptedTransactionDAO.save(new EncryptedTransaction(transactionHash, sanitizedInput));
            LOGGER.info("Stored payload with hash {}", transactionHash);
        }

        return transactionHash;
    }

    @Override
    public void delete(DeleteRequest request) {
        final byte[] hashBytes = base64Codec.decode(request.getKey());
        final MessageHash messageHash = new MessageHash(hashBytes);

        LOGGER.info("Received request to delete message with hash {}", messageHash);
        this.encryptedTransactionDAO.delete(messageHash);
    }

    @Override
    public ReceiveResponse receive(ReceiveRequest request) {

        final byte[] key = base64Codec.decode(request.getKey());

        final Optional<byte[]> to =
                Optional.ofNullable(request.getTo()).filter(str -> !str.isEmpty()).map(base64Codec::decode);

        final MessageHash hash = new MessageHash(key);
        LOGGER.info("Lookup transaction {}", hash);

        if (request.isRaw()) {
            final EncryptedRawTransaction encryptedRawTransaction =
                    encryptedRawTransactionDAO
                            .retrieveByHash(hash)
                            .orElseThrow(
                                    () ->
                                            new TransactionNotFoundException(
                                                    "Raw Message with hash " + hash + " was not found"));

            final RawTransaction rawTransaction =
                    new RawTransaction(
                            encryptedRawTransaction.getEncryptedPayload(),
                            encryptedRawTransaction.getEncryptedKey(),
                            new Nonce(encryptedRawTransaction.getNonce()),
                            PublicKey.from(encryptedRawTransaction.getSender()));

            byte[] response = enclave.unencryptRawPayload(rawTransaction);
            return new ReceiveResponse(response, PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag(), new String[] {}, "");

        } else {
            final EncryptedTransaction encryptedTransaction =
                    encryptedTransactionDAO
                            .retrieveByHash(hash)
                            .orElseThrow(
                                    () ->
                                            new TransactionNotFoundException(
                                                    "Message with hash " + hash + " was not found"));

            final EncodedPayload payload =
                    Optional.of(encryptedTransaction)
                            .map(EncryptedTransaction::getEncodedPayload)
                            .map(payloadEncoder::decode)
                            .orElseThrow(
                                    () -> new IllegalStateException("Unable to decode previously encoded payload"));

            PublicKey recipientKey =
                    to.map(PublicKey::from)
                            .orElse(
                                    searchForRecipientKey(payload)
                                            .orElseThrow(
                                                    () ->
                                                            new RecipientKeyNotFoundException(
                                                                    "No suitable recipient keys found to decrypt payload for : "
                                                                            + hash)));

            byte[] response = enclave.unencryptTransaction(payload, recipientKey);

            final String[] affectedContractTransactions = new String[payload.getAffectedContractTransactions().size()];
            int idx = 0;
            for (TxHash affTxKey : payload.getAffectedContractTransactions().keySet()) {
                affectedContractTransactions[idx++] = base64Codec.encodeToString(affTxKey.getBytes());
            }
            ReceiveResponse result =
                    new ReceiveResponse(
                            response,
                            payload.getPrivacyMode().getPrivacyFlag(),
                            affectedContractTransactions,
                            new String(payload.getExecHash()));

            return result;
        }
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

        RawTransaction rawTransaction =
                enclave.encryptRawPayload(
                        storeRequest.getPayload(),
                        storeRequest.getFrom().map(PublicKey::from).orElseGet(enclave::defaultPublicKey));
        MessageHash hash = messageHashFactory.createFromCipherText(rawTransaction.getEncryptedPayload());

        EncryptedRawTransaction encryptedRawTransaction =
                new EncryptedRawTransaction(
                        hash,
                        rawTransaction.getEncryptedPayload(),
                        rawTransaction.getEncryptedKey(),
                        rawTransaction.getNonce().getNonceBytes(),
                        rawTransaction.getFrom().getKeyBytes());

        encryptedRawTransactionDAO.save(encryptedRawTransaction);

        return new StoreRawResponse(encryptedRawTransaction.getHash().getHashBytes());
    }

    @Override
    public boolean isSender(final String key) {
        final byte[] hashBytes = base64Codec.decode(key);
        final MessageHash hash = new MessageHash(hashBytes);
        final EncodedPayload payload = this.fetchPayload(hash);
        return enclave.getPublicKeys().contains(payload.getSenderKey());
    }

    @Override
    public List<PublicKey> getParticipants(final String ptmHash) {
        final byte[] hashBytes = base64Codec.decode(ptmHash);
        final MessageHash hash = new MessageHash(hashBytes);
        final EncodedPayload payload = this.fetchPayload(hash);

        // this includes the sender
        return payload.getRecipientKeys();
    }

    private EncodedPayload fetchPayload(final MessageHash hash) {
        return encryptedTransactionDAO
                .retrieveByHash(hash)
                .map(EncryptedTransaction::getEncodedPayload)
                .map(payloadEncoder::decode)
                .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));
    }
}
