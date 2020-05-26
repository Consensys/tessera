package com.quorum.tessera.transaction;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrivacyHelperImpl implements PrivacyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyHelperImpl.class);

    private EncryptedTransactionDAO encryptedTransactionDAO;

    public PrivacyHelperImpl(EncryptedTransactionDAO encryptedTransactionDAO) {
        this.encryptedTransactionDAO = encryptedTransactionDAO;
    }

    @Override
    public List<AffectedTransaction> findAffectedContractTransactionsFromSendRequest(String[] affectedHashes) {

        if (Objects.isNull(affectedHashes) || affectedHashes.length == 0) {
            return Collections.emptyList();
        }

        final Set<MessageHash> hashesToFind =
                Arrays.stream(affectedHashes)
                        .map(Base64.getDecoder()::decode)
                        .map(MessageHash::new)
                        .collect(Collectors.toSet());

        final List<EncryptedTransaction> encryptedTransactions = encryptedTransactionDAO.findByHashes(hashesToFind);
        final Set<MessageHash> foundHashes =
            encryptedTransactions.stream().map(EncryptedTransaction::getHash).collect(Collectors.toSet());

        hashesToFind.stream()
                .filter(Predicate.not(foundHashes::contains))
                .findAny()
                .ifPresent(messageHash -> {
                            throw new PrivacyViolationException(
                                    "Unable to find affectedContractTransaction " + messageHash);
                        });

        return encryptedTransactions.stream()
                .map(
                        et ->
                            AffectedTransaction.Builder.create()
                                .withHash(et.getHash().getHashBytes())
                                .withPayload(PayloadEncoder.create().decode(et.getEncodedPayload()))
                                .build()
                ).collect(Collectors.toList());
    }

    @Override
    public List<AffectedTransaction> findAffectedContractTransactionsFromPayload(EncodedPayload payload) {

        final Set<TxHash> affectedTxHashes = payload.getAffectedContractTransactions().keySet();

        if (affectedTxHashes.isEmpty()) {
            return Collections.emptyList();
        }

        final Set<MessageHash> hashesToFind =
                affectedTxHashes.stream().map(TxHash::getBytes).map(MessageHash::new).collect(Collectors.toSet());

        final List<EncryptedTransaction> encryptedTransactions = encryptedTransactionDAO.findByHashes(hashesToFind);
        final Set<MessageHash> foundHashes =
            encryptedTransactions.stream().map(EncryptedTransaction::getHash).collect(Collectors.toSet());

        hashesToFind.stream()
                .filter(Predicate.not(foundHashes::contains))
                .peek(txHash -> LOGGER.debug("Unable to find affectedContractTransaction {}", txHash));

        return encryptedTransactions.stream()
            .map(
                et ->
                    AffectedTransaction.Builder.create()
                        .withHash(et.getHash().getHashBytes())
                        .withPayload(PayloadEncoder.create().decode(et.getEncodedPayload()))
                        .build()
            ).collect(Collectors.toList());
    }

    @Override
    public boolean validateSendRequest(
            PrivacyMode privacyMode, List<PublicKey> recipientList, List<AffectedTransaction> affectedTransactions) {

        if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
            validateRecipients(recipientList, affectedTransactions)
                    .findFirst()
                    .ifPresent(
                            affectedTransaction -> {
                                throw new PrivacyViolationException(
                                        "Recipients mismatched for Affected Txn "
                                                + affectedTransaction.getHash().encodeToBase64());
                            });
        }

        affectedTransactions.stream()
                .filter(a -> a.getPayload().getPrivacyMode() != privacyMode)
                .findFirst()
                .ifPresent(
                        affectedTransaction -> {
                            throw new PrivacyViolationException(
                                    "Private state validation flag mismatched with Affected Txn "
                                            + affectedTransaction.getHash().encodeToBase64());
                        });

        return true;
    }

    @Override
    public boolean validatePayload(
            TxHash txHash, EncodedPayload payload, List<AffectedTransaction> affectedTransactions) {

        final PrivacyMode privacyMode = payload.getPrivacyMode();

        boolean flagMismatched =
                affectedTransactions.stream()
                        .anyMatch(
                                a -> {
                                    if (a.getPayload().getPrivacyMode() != privacyMode) {
                                        LOGGER.info(
                                                "ACOTH {} has PrivacyMode={} for TX {} with PrivacyMode={}. Ignoring transaction.",
                                                a.getHash(),
                                                a.getPayload().getPrivacyMode(),
                                                txHash,
                                                privacyMode.name());
                                        return true;
                                    }
                                    return false;
                                });

        if (flagMismatched) return false;

        if (PrivacyMode.PRIVATE_STATE_VALIDATION == privacyMode) {

            if (affectedTransactions.size() != payload.getAffectedContractTransactions().size()) {
                LOGGER.info("Not all ACOTHs were found. Ignoring transaction.");
                return false;
            }

            final PublicKey senderKey = payload.getSenderKey();

            if (affectedTransactions.stream()
                    .anyMatch(
                            a -> {
                                final List<PublicKey> recipients = a.getPayload().getRecipientKeys();
                                if (!recipients.contains(senderKey)) {
                                    LOGGER.info(
                                            "Sender key {} for TX {} is not a recipient for ACOTH {}",
                                            senderKey.encodeToBase64(),
                                            txHash,
                                            a.getHash().encodeToBase64());
                                    return true;
                                }
                                return false;
                            })) {
                return false;
            }

            validateRecipients(payload.getRecipientKeys(), affectedTransactions)
                    .findFirst()
                    .ifPresent(
                            affectedTransaction -> {
                                throw new PrivacyViolationException(
                                        "Recipients mismatched for Affected Txn "
                                                + affectedTransaction.getHash().encodeToBase64());
                            });
        }

        return true;
    }

    @Override
    public EncodedPayload sanitisePrivacyPayload(
            TxHash txHash, EncodedPayload encodedPayload, Set<TxHash> invalidSecurityHashes) {

        if (PrivacyMode.PRIVATE_STATE_VALIDATION == encodedPayload.getPrivacyMode()) {
            throw new PrivacyViolationException(
                    "Invalid security hashes identified for PSC TX "
                            + txHash
                            + ". Invalid ACOTHs: "
                            + invalidSecurityHashes.stream()
                                    .map(TxHash::encodeToBase64)
                                    .collect(Collectors.joining(",")));
        }

        Map<TxHash, byte[]> affectedTxs =
                encodedPayload.getAffectedContractTransactions().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getData()));

        invalidSecurityHashes.forEach(txKey -> affectedTxs.remove(txKey));

        final EncodedPayload sanitisedPayload =
                EncodedPayload.Builder.from(encodedPayload).withAffectedContractTransactions(affectedTxs).build();

        LOGGER.debug(
                "A number of security hashes are invalid and have been discarded for transaction with hash {}. Invalid affected contract transaction hashes: {}",
                txHash,
                invalidSecurityHashes.stream().map(TxHash::encodeToBase64).collect(Collectors.joining(",")));

        return sanitisedPayload;
    }

    /*
       Stream of invalid recipients (for reporting/logging)
    */
    private static Stream<AffectedTransaction> validateRecipients(
            List<PublicKey> recipientList, List<AffectedTransaction> affectedContractTransactions) {

        Predicate<AffectedTransaction> payloadRecipientsHasAllRecipients =
                a -> a.getPayload().getRecipientKeys().containsAll(recipientList);
        Predicate<AffectedTransaction> recipientsHaveAllPayloadRecipients =
                a -> recipientList.containsAll(a.getPayload().getRecipientKeys());
        Predicate<AffectedTransaction> allRecipientsMatch =
                payloadRecipientsHasAllRecipients.and(recipientsHaveAllPayloadRecipients);

        return affectedContractTransactions.stream().filter(allRecipientsMatch.negate());
    }
}
