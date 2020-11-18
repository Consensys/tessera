package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

class EncodedPayloadManagerImpl implements EncodedPayloadManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncodedPayloadManagerImpl.class);

    private final Enclave enclave;

    private final PrivacyHelper privacyHelper;

    private final MessageHashFactory messageHashFactory;

    EncodedPayloadManagerImpl(final Enclave enclave,
                                     final PrivacyHelper privacyHelper,
                                     final MessageHashFactory mhFactory) {
        this.enclave = Objects.requireNonNull(enclave);
        this.privacyHelper = Objects.requireNonNull(privacyHelper);
        this.messageHashFactory = Objects.requireNonNull(mhFactory);
    }

    @Override
    public EncodedPayload create(final SendRequest request) {
        final PublicKey senderPublicKey = request.getSender();
        LOGGER.debug("Sender for payload: {}", request.getSender().encodeToBase64());

        final Set<PublicKey> recipientSet = new HashSet<>();
        recipientSet.add(senderPublicKey);
        recipientSet.addAll(request.getRecipients());
        recipientSet.addAll(enclave.getForwardingKeys());

        final List<PublicKey> recipientListNoDuplicate = new ArrayList<>(recipientSet);
        LOGGER.debug("Recipients for payload: {}", recipientListNoDuplicate);

        final PrivacyMode privacyMode = request.getPrivacyMode();
        LOGGER.debug("Privacy mode for payload: {}", request.getPrivacyMode());
        LOGGER.debug("ExecHash for payload: {}", new String(request.getExecHash()));

        final List<AffectedTransaction> affectedContractTransactions =
            privacyHelper.findAffectedContractTransactionsFromSendRequest(request.getAffectedContractTransactions());

        LOGGER.debug("Validating request against affected contracts");
        privacyHelper.validateSendRequest(privacyMode, recipientListNoDuplicate, affectedContractTransactions);
        LOGGER.debug("Successful validation against affected contracts");

        return enclave.encryptPayload(
            request.getPayload(), senderPublicKey, recipientListNoDuplicate,
            privacyMode, affectedContractTransactions, request.getExecHash()
        );
    }

    @Override
    public ReceiveResponse decrypt(final EncodedPayload payload, final PublicKey maybeDefaultRecipient) {
        final MessageHash customPayloadHash = messageHashFactory.createFromCipherText(payload.getCipherText());
        LOGGER.debug("Decrypt request for custom message with hash {}", customPayloadHash);

        final PublicKey recipientKey =
            Optional.ofNullable(maybeDefaultRecipient)
                .orElseGet(() -> searchForRecipientKey(payload).orElseThrow(() -> new RecipientKeyNotFoundException("No suitable recipient keys found to decrypt payload for " + customPayloadHash)));
        LOGGER.debug("Decryption key found: {}", recipientKey.encodeToBase64());

        final byte[] decryptedTransactionData = enclave.unencryptTransaction(payload, recipientKey);

        final Set<MessageHash> txns = payload
            .getAffectedContractTransactions()
            .keySet()
            .stream()
            .map(TxHash::getBytes)
            .map(MessageHash::new)
            .collect(Collectors.toSet());

        return ReceiveResponse.Builder.create()
            .withUnencryptedTransactionData(decryptedTransactionData)
            .withPrivacyMode(payload.getPrivacyMode())
            .withAffectedTransactions(txns)
            .withExecHash(payload.getExecHash())
            .build();
    }

    private Optional<PublicKey> searchForRecipientKey(final EncodedPayload payload) {
        final MessageHash customPayloadHash = messageHashFactory.createFromCipherText(payload.getCipherText());

        for (final PublicKey potentialMatchingKey : enclave.getPublicKeys()) {
            try {
                LOGGER.debug("Attempting to decrypt {} using key {}", customPayloadHash, potentialMatchingKey.encodeToBase64());
                enclave.unencryptTransaction(payload, potentialMatchingKey);
                LOGGER.debug("Succeeded decrypting {} using key {}", customPayloadHash, potentialMatchingKey.encodeToBase64());
                return Optional.of(potentialMatchingKey);
            } catch (EnclaveException | IndexOutOfBoundsException | EncryptorException ex) {
                LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
            }
        }
        return Optional.empty();
    }
}
