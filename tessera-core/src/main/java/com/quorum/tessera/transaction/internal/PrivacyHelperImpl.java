package com.quorum.tessera.transaction.internal;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.PrivacyHelper;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivacyHelperImpl implements PrivacyHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyHelperImpl.class);

  private final EncryptedTransactionDAO encryptedTransactionDAO;

  private final boolean isEnhancedPrivacyEnabled;

  public PrivacyHelperImpl(
      EncryptedTransactionDAO encryptedTransactionDAO, boolean isEnhancedPrivacyEnabled) {
    this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
    this.isEnhancedPrivacyEnabled = isEnhancedPrivacyEnabled;
  }

  @Override
  public List<AffectedTransaction> findAffectedContractTransactionsFromSendRequest(
      Set<MessageHash> affectedHashes) {

    if (Objects.isNull(affectedHashes) || affectedHashes.isEmpty()) {
      return Collections.emptyList();
    }

    final List<EncryptedTransaction> encryptedTransactions =
        encryptedTransactionDAO.findByHashes(affectedHashes);
    final Set<MessageHash> foundHashes =
        encryptedTransactions.stream()
            .map(EncryptedTransaction::getHash)
            .collect(Collectors.toSet());

    affectedHashes.stream()
        .filter(Predicate.not(foundHashes::contains))
        .findAny()
        .ifPresent(
            messageHash -> {
              throw new PrivacyViolationException(
                  "Unable to find affectedContractTransaction " + messageHash);
            });

    return encryptedTransactions.stream()
        .map(
            et ->
                AffectedTransaction.Builder.create()
                    .withHash(et.getHash().getHashBytes())
                    .withPayload(et.getPayload())
                    .build())
        .collect(Collectors.toList());
  }

  @Override
  public List<AffectedTransaction> findAffectedContractTransactionsFromPayload(
      EncodedPayload payload) {

    final Set<TxHash> affectedTxHashes = payload.getAffectedContractTransactions().keySet();

    if (affectedTxHashes.isEmpty()) {
      return Collections.emptyList();
    }

    final Set<MessageHash> hashesToFind =
        affectedTxHashes.stream()
            .map(TxHash::getBytes)
            .map(MessageHash::new)
            .collect(Collectors.toSet());

    final List<EncryptedTransaction> encryptedTransactions =
        encryptedTransactionDAO.findByHashes(hashesToFind);
    final Set<MessageHash> foundHashes =
        encryptedTransactions.stream()
            .map(EncryptedTransaction::getHash)
            .collect(Collectors.toSet());

    hashesToFind.stream()
        .filter(Predicate.not(foundHashes::contains))
        .forEach(txHash -> LOGGER.debug("Unable to find affectedContractTransaction {}", txHash));

    return encryptedTransactions.stream()
        .map(
            et ->
                AffectedTransaction.Builder.create()
                    .withHash(et.getHash().getHashBytes())
                    .withPayload(et.getPayload())
                    .build())
        .collect(Collectors.toList());
  }

  @Override
  public boolean validateSendRequest(
      PrivacyMode privacyMode,
      List<PublicKey> recipientList,
      List<AffectedTransaction> affectedTransactions,
      Set<PublicKey> mandatoryRecipients) {

    if (privacyMode != PrivacyMode.STANDARD_PRIVATE) {
      checkIfEnhancedPrivacyIsEnabled();
    }

    if (privacyMode == PrivacyMode.MANDATORY_RECIPIENTS
        && !recipientList.containsAll(mandatoryRecipients)) {
      throw new PrivacyViolationException(
          "One or more mandatory recipients not included in the participant list");
    }

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
        .filter(
            a ->
                a.getPayload().getPrivacyMode() != privacyMode
                    || !mandatoryRecipients.containsAll(a.getPayload().getMandatoryRecipients()))
        .findFirst()
        .ifPresent(
            affectedTransaction -> {
              throw new PrivacyViolationException(
                  "Privacy metadata mismatched with Affected Txn "
                      + affectedTransaction.getHash().encodeToBase64());
            });

    return true;
  }

  @Override
  public boolean validatePayload(
      TxHash txHash, EncodedPayload payload, List<AffectedTransaction> affectedTransactions) {

    final PrivacyMode privacyMode = payload.getPrivacyMode();

    if (privacyMode != PrivacyMode.STANDARD_PRIVATE) {
      checkIfEnhancedPrivacyIsEnabled();
    }

    boolean privacyMetadataMismatched =
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
                  if (!payload
                      .getMandatoryRecipients()
                      .containsAll(a.getPayload().getMandatoryRecipients())) {
                    LOGGER.info(
                        "ACOTH {} has mandatory recipients mismatched. Ignoring transaction.",
                        a.getHash());
                    return true;
                  }
                  return false;
                });

    if (privacyMetadataMismatched) return false;

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
        EncodedPayload.Builder.from(encodedPayload)
            .withAffectedContractTransactions(affectedTxs)
            .build();

    LOGGER.debug(
        "A number of security hashes are invalid and have been discarded for transaction with hash {}. Invalid affected contract transaction hashes: {}",
        txHash,
        invalidSecurityHashes.stream()
            .map(TxHash::encodeToBase64)
            .collect(Collectors.joining(",")));

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

  private boolean checkIfEnhancedPrivacyIsEnabled() {

    if (!isEnhancedPrivacyEnabled) {
      throw new EnhancedPrivacyNotSupportedException("Enhanced Privacy is not enabled");
    }

    return true;
  }
}
