package com.quorum.tessera.transaction;

import com.quorum.tessera.data.*;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.util.Base64Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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

    private final BatchPayloadPublisher batchPayloadPublisher;

    private final Enclave enclave;

    private final ResendManager resendManager;

    private final PrivacyHelper privacyHelper;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    public TransactionManagerImpl(
            EncryptedTransactionDAO encryptedTransactionDAO,
            Enclave enclave,
            EncryptedRawTransactionDAO encryptedRawTransactionDAO,
            ResendManager resendManager,
            BatchPayloadPublisher batchPayloadPublisher,
            PrivacyHelper privacyHelper) {
        this(
                Base64Codec.create(),
                PayloadEncoder.create(),
                encryptedTransactionDAO,
                batchPayloadPublisher,
                enclave,
                encryptedRawTransactionDAO,
                resendManager,
                privacyHelper);
    }

    // Only use for tests
    public TransactionManagerImpl(
            Base64Codec base64Decoder,
            PayloadEncoder payloadEncoder,
            EncryptedTransactionDAO encryptedTransactionDAO,
            BatchPayloadPublisher batchPayloadPublisher,
            Enclave enclave,
            EncryptedRawTransactionDAO encryptedRawTransactionDAO,
            ResendManager resendManager,
            PrivacyHelper privacyHelper) {

        this.base64Codec = Objects.requireNonNull(base64Decoder, "base64Codec is required");
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder, "payloadEncoder is required");
        this.encryptedTransactionDAO =
                Objects.requireNonNull(encryptedTransactionDAO, "encryptedTransactionDAO is required");
        this.batchPayloadPublisher = Objects.requireNonNull(batchPayloadPublisher, "batchPayloadPublisher is required");
        this.enclave = Objects.requireNonNull(enclave, "enclave is required");
        this.encryptedRawTransactionDAO =
                Objects.requireNonNull(encryptedRawTransactionDAO, "encryptedRawTransactionDAO is required");
        this.resendManager = Objects.requireNonNull(resendManager, "resendManager is required");
        this.privacyHelper = Objects.requireNonNull(privacyHelper, "privacyHelper is required");
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

        final PrivacyMode privacyMode = sendRequest.getPrivacyMode();

        final byte[] execHash = sendRequest.getExecHash();

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

        byte[] payloadData = this.payloadEncoder.encode(payload);
        final EncryptedTransaction newTransaction = new EncryptedTransaction(transactionHash, payloadData);

        final List<PublicKey> recipientListRemotesOnly =
                recipientListNoDuplicate.stream()
                        .filter(k -> !enclave.getPublicKeys().contains(k))
                        .collect(Collectors.toList());

        this.encryptedTransactionDAO.save(
                newTransaction,
                () -> {
                    batchPayloadPublisher.publishPayload(payload, recipientListRemotesOnly);
                    return null;
                });

        return SendResponse.from(transactionHash);
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

        final PrivacyMode privacyMode = sendRequest.getPrivacyMode();

        final byte[] execHash = sendRequest.getExecHash();

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

        final List<PublicKey> recipientListRemotesOnly =
                recipientListNoDuplicate.stream()
                        .filter(k -> !enclave.getPublicKeys().contains(k))
                        .collect(Collectors.toList());

        this.encryptedTransactionDAO.save(
                newTransaction,
                () -> {
                    batchPayloadPublisher.publishPayload(payload, recipientListRemotesOnly);
                    return null;
                });

        return SendResponse.from(messageHash);
    }

    @Override
    public MessageHash storePayload(final EncodedPayload payload) {

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

        final EncodedPayload encodedPayload;
        if (!invalidSecurityHashes.isEmpty()) {
            encodedPayload =
                    privacyHelper.sanitisePrivacyPayload(
                            TxHash.from(transactionHash.getHashBytes()), payload, invalidSecurityHashes);
        } else {
            encodedPayload = payload;
        }
        LOGGER.debug(
                "AffectedContractTransaction.size={} InvalidSecurityHashes.size={}",
                affectedContractTransactions.size(),
                invalidSecurityHashes.size());

        if (enclave.getPublicKeys().contains(payload.getSenderKey())) {
            this.resendManager.acceptOwnMessage(encodedPayload);

        } else {

            // this is a tx from someone else
            byte[] payloadData = payloadEncoder.encode(encodedPayload);
            this.encryptedTransactionDAO.save(new EncryptedTransaction(transactionHash, payloadData));
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
            return ReceiveResponse.Builder.create()
                    .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                    .withUnencryptedTransactionData(response)
                    .build();

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
                    request.getRecipient()
                            .orElse(
                                    searchForRecipientKey(payload)
                                            .orElseThrow(
                                                    () ->
                                                            new RecipientKeyNotFoundException(
                                                                    "No suitable recipient keys found to decrypt payload for : "
                                                                            + hash)));

            byte[] unencryptedTransactionData = enclave.unencryptTransaction(payload, recipientKey);

            Set<MessageHash> txns =
                    payload.getAffectedContractTransactions().keySet().stream()
                            .map(TxHash::getBytes)
                            .map(MessageHash::new)
                            .collect(Collectors.toSet());

            return ReceiveResponse.Builder.create()
                    .withUnencryptedTransactionData(unencryptedTransactionData)
                    .withPrivacyMode(payload.getPrivacyMode())
                    .withAffectedTransactions(txns)
                    .withExecHash(payload.getExecHash())
                    .build();
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
    public boolean upcheck() {
        if (!encryptedRawTransactionDAO.upcheck()) {
            return false;
        }
        if (!encryptedTransactionDAO.upcheck()) {
            return false;
        }
        return true;
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
