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
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.util.Base64Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
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

    private final Base64Decoder base64Decoder;

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
                Base64Decoder.create(),
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
            Base64Decoder base64Decoder,
            PayloadEncoder payloadEncoder,
            EncryptedTransactionDAO encryptedTransactionDAO,
            PartyInfoService partyInfoService,
            Enclave enclave,
            EncryptedRawTransactionDAO encryptedRawTransactionDAO,
            ResendManager resendManager,
            int resendFetchSize) {

        this.base64Decoder = Objects.requireNonNull(base64Decoder, "base64Decoder is required");
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
    @Transactional
    public SendResponse send(SendRequest sendRequest) {

        final String sender = sendRequest.getFrom();

        final PublicKey senderPublicKey =
                Optional.ofNullable(sender)
                        .map(base64Decoder::decode)
                        .map(PublicKey::from)
                        .orElseGet(enclave::defaultPublicKey);

        final byte[][] recipients =
                Stream.of(sendRequest)
                        .filter(sr -> Objects.nonNull(sr.getTo()))
                        .flatMap(s -> Stream.of(s.getTo()))
                        .map(base64Decoder::decode)
                        .toArray(byte[][]::new);

        final List<PublicKey> recipientList = Stream.of(recipients).map(PublicKey::from).collect(Collectors.toList());

        recipientList.add(senderPublicKey);

        recipientList.addAll(enclave.getForwardingKeys());

        final List<PublicKey> recipientListNoDuplicate = recipientList.stream().distinct().collect(Collectors.toList());

        final byte[] raw = sendRequest.getPayload();

        final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendRequest.getPrivacyFlag());

        final byte[] execHash =
                Optional.ofNullable(sendRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

        final Map<TxHash, EncodedPayload> affectedContractTransactions =
                buildAffectedContractTransactions(privacyMode, sendRequest.getAffectedContractTransactions());

        validatePrivacyMode(Optional.empty(), privacyMode, affectedContractTransactions);

        if (PrivacyMode.PRIVATE_STATE_VALIDATION == privacyMode) {
            validateRecipients(Optional.empty(), recipientList, affectedContractTransactions);
        }

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

        final String encodedKey = base64Decoder.encodeToString(key);

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
    @Transactional
    public SendResponse sendSignedTransaction(final SendSignedRequest sendRequest) {

        final byte[][] recipients =
                Stream.of(sendRequest)
                        .filter(sr -> Objects.nonNull(sr.getTo()))
                        .flatMap(s -> Stream.of(s.getTo()))
                        .map(base64Decoder::decode)
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

        final Map<TxHash, EncodedPayload> affectedContractTransactions =
                buildAffectedContractTransactions(privacyMode, sendRequest.getAffectedContractTransactions());

        final byte[] execHash =
                Optional.ofNullable(sendRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

        validatePrivacyMode(Optional.empty(), privacyMode, affectedContractTransactions);

        if (PrivacyMode.PRIVATE_STATE_VALIDATION == privacyMode) {
            validateRecipients(Optional.of(messageHash), recipientList, affectedContractTransactions);
        }

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

        final String encodedKey = base64Decoder.encodeToString(key);

        return new SendResponse(encodedKey);
    }

    @Override
    public ResendResponse resend(ResendRequest request) {

        final byte[] publicKeyData = base64Decoder.decode(request.getPublicKey());
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

            final byte[] hashKey = base64Decoder.decode(request.getKey());
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
                payload.getRecipientKeys().add(decryptedKey);
                returnValue = payload;
            } else {
                // this is our tx
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

        final PrivacyMode privacyMode = payload.getPrivacyMode();

        final Map<TxHash, EncodedPayload> affectedContractTransactions =
                buildAffectedContractTransactions(privacyMode, payload.getAffectedContractTransactions().keySet());

        if (!validatePrivacyMode(Optional.of(transactionHash), privacyMode, affectedContractTransactions)) {
            return transactionHash;
        }
        if (PrivacyMode.PRIVATE_STATE_VALIDATION == privacyMode) {
            if (!validateIfSenderIsGenuine(transactionHash, payload, affectedContractTransactions)) {
                return transactionHash;
            }
            validateRecipients(Optional.of(transactionHash), payload.getRecipientKeys(), affectedContractTransactions);
        }

        final Set<TxHash> invalidSecurityHashes =
                enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);

        byte[] sanitizedInput = input;
        if (!invalidSecurityHashes.isEmpty()) {
            if (PrivacyMode.PRIVATE_STATE_VALIDATION == privacyMode) {
                throw new PrivacyViolationException(
                        "Invalid security hashes identified for PSC TX "
                                + base64Decoder.encodeToString(transactionHash.getHashBytes())
                                + ". Invalid ACOTHs: "
                                + invalidSecurityHashes.stream()
                                        .map(TxHash::encodeToBase64)
                                        .collect(Collectors.joining(",")));
            }
            invalidSecurityHashes.forEach(txKey -> payload.getAffectedContractTransactions().remove(txKey));
            LOGGER.debug(
                    "A number of security hashes are invalid and have been discarded for transaction with hash {}. Invalid affected contract transaction hashes: {}",
                    base64Decoder.encodeToString(transactionHash.getHashBytes()),
                    invalidSecurityHashes.stream().map(TxHash::encodeToBase64).collect(Collectors.joining(",")));
            sanitizedInput = payloadEncoder.encode(payload);
        }
        // TODO - remove extra logs
        LOGGER.info(
                "AffectedContractTransaction.size={} InvalidSecurityHashes.size={}",
                affectedContractTransactions.size(),
                invalidSecurityHashes.size());

        if (enclave.getPublicKeys().contains(payload.getSenderKey())) {

            this.resendManager.acceptOwnMessage(input);

        } else {

            // this is a tx from someone else
            this.encryptedTransactionDAO.save(new EncryptedTransaction(transactionHash, input));
            LOGGER.info("Stored payload with hash {}", transactionHash);
        }

        return transactionHash;
    }

    @Override
    @Transactional
    public void delete(DeleteRequest request) {
        final byte[] hashBytes = base64Decoder.decode(request.getKey());
        final MessageHash messageHash = new MessageHash(hashBytes);

        LOGGER.info("Received request to delete message with hash {}", messageHash);
        this.encryptedTransactionDAO.delete(messageHash);
    }

    @Override
    @Transactional
    public ReceiveResponse receive(ReceiveRequest request) {

        final byte[] key = base64Decoder.decode(request.getKey());

        final Optional<byte[]> to =
                Optional.ofNullable(request.getTo()).filter(str -> !str.isEmpty()).map(base64Decoder::decode);

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
                affectedContractTransactions[idx++] = base64Decoder.encodeToString(affTxKey.getBytes());
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
    @Transactional
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

    private Map<TxHash, EncodedPayload> buildAffectedContractTransactions(
            PrivacyMode privacyMode, String[] affectedContractTransactionsList) {
        if (Objects.isNull(affectedContractTransactionsList)) {
            return Collections.emptyMap();
        }
        final Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        for (String affTxHashB64 : affectedContractTransactionsList) {
            MessageHash affTxHash = new MessageHash(base64Decoder.decode(affTxHashB64));
            Optional<EncryptedTransaction> affTx = this.encryptedTransactionDAO.retrieveByHash(affTxHash);
            if (affTx.isPresent()) {
                affectedContractTransactions.put(
                        new TxHash(affTxHash.getHashBytes()), payloadEncoder.decode(affTx.get().getEncodedPayload()));
            } else {
                throw new PrivacyViolationException("Unable to find affectedContractTransaction " + affTxHashB64);
            }
        }
        return affectedContractTransactions;
    }

    private Map<TxHash, EncodedPayload> buildAffectedContractTransactions(
            PrivacyMode privacyMode, Set<TxHash> txHashes) {
        if (Objects.isNull(txHashes) || txHashes.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<TxHash, EncodedPayload> affectedContractTransactions = new HashMap<>();
        for (TxHash txHash : txHashes) {
            MessageHash affTxHash = new MessageHash(txHash.getBytes());
            Optional<EncryptedTransaction> affTx = this.encryptedTransactionDAO.retrieveByHash(affTxHash);
            if (affTx.isPresent()) {
                affectedContractTransactions.put(
                        new TxHash(affTxHash.getHashBytes()), payloadEncoder.decode(affTx.get().getEncodedPayload()));
            } else {
                LOGGER.debug("Unable to find affectedContractTransaction {}", txHash.encodeToBase64());
            }
        }
        return affectedContractTransactions;
    }

    private boolean validatePrivacyMode(
            Optional<MessageHash> txHash,
            PrivacyMode privacyMode,
            Map<TxHash, EncodedPayload> affectedContractTransactions) {
        boolean result = true;
        for (Map.Entry<TxHash, EncodedPayload> entry : affectedContractTransactions.entrySet()) {
            final PrivacyMode affectedContractPrivacyMode = entry.getValue().getPrivacyMode();
            if (affectedContractPrivacyMode != privacyMode) {
                if (!txHash.isPresent()) {
                    throw new PrivacyViolationException(
                            "Private state validation flag mismatched with Affected Txn "
                                    + entry.getKey().encodeToBase64());
                } else {
                    LOGGER.info(
                            "ACOTH {} has PrivacyMode={} for TX {} with PrivacyMode={}. Ignoring transaction.",
                            entry.getKey().encodeToBase64(),
                            affectedContractPrivacyMode.name(),
                            base64Decoder.encodeToString(txHash.get().getHashBytes()),
                            privacyMode.name());
                    result = false;
                }
            }
        }
        return result;
    }

    private boolean validateRecipients(
            Optional<MessageHash> txHash,
            List<PublicKey> recipientList,
            Map<TxHash, EncodedPayload> affectedContractTransactions) {
        for (Map.Entry<TxHash, EncodedPayload> entry : affectedContractTransactions.entrySet()) {
            if (!entry.getValue().getRecipientKeys().containsAll(recipientList)
                    || !recipientList.containsAll(entry.getValue().getRecipientKeys())) {
                throw new PrivacyViolationException(
                        "Recipients mismatched for Affected Txn "
                                + entry.getKey().encodeToBase64()
                                + ". TxHash="
                                + txHash.map(MessageHash::getHashBytes)
                                        .map(base64Decoder::encodeToString)
                                        .orElse("NONE"));
            }
        }
        return true;
    }

    private boolean validateIfSenderIsGenuine(
            MessageHash txHash, EncodedPayload payload, Map<TxHash, EncodedPayload> affectedContractTransactions) {
        boolean result = true;
        if (affectedContractTransactions.size() != payload.getAffectedContractTransactions().size()) {
            // This could be a recipient discovery attack. Respond successfully while not saving the transaction.
            LOGGER.info(
                    "Not all ACOTHs were found for inbound TX {}. Ignoring transaction.",
                    base64Decoder.encodeToString(txHash.getHashBytes()));
            return false;
        }
        final PublicKey senderKey = payload.getSenderKey();
        for (Map.Entry<TxHash, EncodedPayload> entry : affectedContractTransactions.entrySet()) {
            if (!entry.getValue().getRecipientKeys().contains(senderKey)) {
                LOGGER.info(
                        "Sender key {} for TX {} is not a recipient for ACOTH {}",
                        senderKey.encodeToBase64(),
                        base64Decoder.encodeToString(txHash.getHashBytes()),
                        entry.getKey().encodeToBase64());
                result = false;
            }
        }

        return result;
    }

    @Override
    @Transactional
    public boolean isSender(final String key) {
        final byte[] hashBytes = base64Decoder.decode(key);
        final MessageHash hash = new MessageHash(hashBytes);
        final EncodedPayload payload = this.fetchPayload(hash);
        return enclave.getPublicKeys().contains(payload.getSenderKey());
    }

    @Override
    @Transactional
    public List<PublicKey> getParticipants(final String ptmHash) {
        final byte[] hashBytes = base64Decoder.decode(ptmHash);
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
