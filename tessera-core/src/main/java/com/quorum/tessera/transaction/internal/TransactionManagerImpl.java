package com.quorum.tessera.transaction.internal;

import static java.util.function.Predicate.not;

import com.quorum.tessera.data.*;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.*;
import com.quorum.tessera.transaction.exception.MandatoryRecipientsNotAvailableException;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.resend.ResendManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManagerImpl implements TransactionManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerImpl.class);

  private final EncryptedTransactionDAO encryptedTransactionDAO;

  private final EncryptedRawTransactionDAO encryptedRawTransactionDAO;

  private final BatchPayloadPublisher batchPayloadPublisher;

  private final Enclave enclave;

  private final ResendManager resendManager;

  private final PrivacyHelper privacyHelper;

  private final PayloadDigest payloadDigest;

  public TransactionManagerImpl(
      Enclave enclave,
      EncryptedTransactionDAO encryptedTransactionDAO,
      EncryptedRawTransactionDAO encryptedRawTransactionDAO,
      ResendManager resendManager,
      BatchPayloadPublisher batchPayloadPublisher,
      PrivacyHelper privacyHelper,
      PayloadDigest payloadDigest) {
    this.encryptedTransactionDAO =
        Objects.requireNonNull(encryptedTransactionDAO, "encryptedTransactionDAO is required");
    this.batchPayloadPublisher =
        Objects.requireNonNull(batchPayloadPublisher, "batchPayloadPublisher is required");
    this.enclave = Objects.requireNonNull(enclave, "enclave is required");
    this.encryptedRawTransactionDAO =
        Objects.requireNonNull(
            encryptedRawTransactionDAO, "encryptedRawTransactionDAO is required");
    this.resendManager = Objects.requireNonNull(resendManager, "resendManager is required");
    this.privacyHelper = Objects.requireNonNull(privacyHelper, "privacyHelper is required");
    this.payloadDigest = Objects.requireNonNull(payloadDigest, "payloadDigest is required");
  }

  @Override
  public SendResponse send(SendRequest sendRequest) {

    final PublicKey senderPublicKey = sendRequest.getSender();
    final List<PublicKey> recipientList = new ArrayList<>(sendRequest.getRecipients());
    recipientList.add(senderPublicKey);
    recipientList.addAll(enclave.getForwardingKeys());

    final List<PublicKey> recipientListNoDuplicate =
        recipientList.stream().distinct().collect(Collectors.toList());

    final byte[] raw = sendRequest.getPayload();

    final PrivacyMode privacyMode = sendRequest.getPrivacyMode();

    final byte[] execHash = sendRequest.getExecHash();

    final List<AffectedTransaction> affectedContractTransactions =
        privacyHelper.findAffectedContractTransactionsFromSendRequest(
            sendRequest.getAffectedContractTransactions());

    privacyHelper.validateSendRequest(
        privacyMode,
        recipientList,
        affectedContractTransactions,
        sendRequest.getMandatoryRecipients());

    final PrivacyMetadata.Builder metadataBuilder =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(privacyMode)
            .withAffectedTransactions(affectedContractTransactions)
            .withExecHash(execHash)
            .withMandatoryRecipients(sendRequest.getMandatoryRecipients());
    sendRequest.getPrivacyGroupId().ifPresent(metadataBuilder::withPrivacyGroupId);

    final EncodedPayload payload =
        enclave.encryptPayload(
            raw, senderPublicKey, recipientListNoDuplicate, metadataBuilder.build());

    final MessageHash transactionHash =
        Optional.of(payload)
            .map(EncodedPayload::getCipherText)
            .map(payloadDigest::digest)
            .map(MessageHash::new)
            .get();

    final EncryptedTransaction newTransaction = new EncryptedTransaction(transactionHash, payload);

    final Set<PublicKey> managedPublicKeys = enclave.getPublicKeys();
    final Set<PublicKey> managedParties =
        Stream.concat(Stream.of(senderPublicKey), recipientListNoDuplicate.stream())
            .filter(managedPublicKeys::contains)
            .collect(Collectors.toSet());

    final List<PublicKey> recipientListRemotesOnly =
        recipientListNoDuplicate.stream()
            .filter(not(managedPublicKeys::contains))
            .collect(Collectors.toList());

    this.encryptedTransactionDAO.save(
        newTransaction,
        () -> {
          batchPayloadPublisher.publishPayload(payload, recipientListRemotesOnly);
          return null;
        });

    return SendResponse.Builder.create()
        .withMessageHash(transactionHash)
        .withManagedParties(managedParties)
        .withSender(payload.getSenderKey())
        .build();
  }

  @Override
  public SendResponse sendSignedTransaction(final SendSignedRequest sendRequest) {

    final List<PublicKey> recipientList = new ArrayList<>(sendRequest.getRecipients());
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

    privacyHelper.validateSendRequest(
        privacyMode,
        recipientList,
        affectedContractTransactions,
        sendRequest.getMandatoryRecipients());

    final List<PublicKey> recipientListNoDuplicate =
        recipientList.stream().distinct().collect(Collectors.toList());

    final PrivacyMetadata.Builder privacyMetaDataBuilder =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(privacyMode)
            .withAffectedTransactions(affectedContractTransactions)
            .withExecHash(execHash)
            .withMandatoryRecipients(sendRequest.getMandatoryRecipients());
    sendRequest.getPrivacyGroupId().ifPresent(privacyMetaDataBuilder::withPrivacyGroupId);

    final EncodedPayload payload =
        enclave.encryptPayload(
            encryptedRawTransaction.toRawTransaction(),
            recipientListNoDuplicate,
            privacyMetaDataBuilder.build());

    final EncryptedTransaction newTransaction = new EncryptedTransaction(messageHash, payload);

    final Set<PublicKey> managedPublicKeys = enclave.getPublicKeys();
    final Set<PublicKey> managedParties =
        recipientListNoDuplicate.stream()
            .filter(managedPublicKeys::contains)
            .collect(Collectors.toSet());

    final List<PublicKey> recipientListRemotesOnly =
        recipientListNoDuplicate.stream()
            .filter(not(managedPublicKeys::contains))
            .collect(Collectors.toList());

    this.encryptedTransactionDAO.save(
        newTransaction,
        () -> {
          batchPayloadPublisher.publishPayload(payload, recipientListRemotesOnly);
          return null;
        });

    return SendResponse.Builder.create()
        .withMessageHash(messageHash)
        .withManagedParties(managedParties)
        .withSender(PublicKey.from(encryptedRawTransaction.getSender()))
        .build();
  }

  @Override
  public synchronized MessageHash storePayload(final EncodedPayload payload) {

    final byte[] digest = payloadDigest.digest(payload.getCipherText());
    final MessageHash transactionHash = new MessageHash(digest);
    final List<AffectedTransaction> affectedContractTransactions =
        privacyHelper.findAffectedContractTransactionsFromPayload(payload);

    final TxHash txHash = TxHash.from(transactionHash.getHashBytes());
    if (!privacyHelper.validatePayload(txHash, payload, affectedContractTransactions)) {
      return transactionHash;
    }

    final Set<TxHash> invalidSecurityHashes =
        enclave.findInvalidSecurityHashes(payload, affectedContractTransactions);

    final EncodedPayload encodedPayload;
    if (!invalidSecurityHashes.isEmpty()) {
      encodedPayload = privacyHelper.sanitisePrivacyPayload(txHash, payload, invalidSecurityHashes);
    } else {
      encodedPayload = payload;
    }
    LOGGER.debug(
        "AffectedContractTransaction.size={} InvalidSecurityHashes.size={}",
        affectedContractTransactions.size(),
        invalidSecurityHashes.size());

    // Validations are complete, now we need to save it to the database

    if (enclave.getPublicKeys().contains(encodedPayload.getSenderKey())) {
      // This is our own message that we are rebuilding, handle separately
      this.resendManager.acceptOwnMessage(encodedPayload);
      LOGGER.debug("Stored payload for which we were the sender. Hash = {}", transactionHash);
      return transactionHash;
    }

    // This is a transaction with a different node as the sender
    final Optional<EncryptedTransaction> tx =
        this.encryptedTransactionDAO.retrieveByHash(transactionHash);
    if (tx.isEmpty()) {
      // This is the first time we have seen the payload, so just save it to the database as is
      this.encryptedTransactionDAO.save(new EncryptedTransaction(transactionHash, encodedPayload));
      LOGGER.debug("Stored new payload with hash {}", transactionHash);
      return transactionHash;
    }

    final EncryptedTransaction encryptedTransaction = tx.get();
    final EncodedPayload existing = encryptedTransaction.getPayload();

    // check all the other bits of the payload match
    final boolean txMatches =
        Stream.of(
                Arrays.equals(existing.getCipherText(), encodedPayload.getCipherText()),
                Objects.equals(existing.getCipherTextNonce(), encodedPayload.getCipherTextNonce()),
                Objects.equals(existing.getSenderKey(), encodedPayload.getSenderKey()),
                Objects.equals(existing.getRecipientNonce(), encodedPayload.getRecipientNonce()),
                Objects.equals(existing.getPrivacyMode(), encodedPayload.getPrivacyMode()),
                Arrays.equals(existing.getExecHash(), encodedPayload.getExecHash()),

                // checks the affected contracts contents match
                Objects.equals(
                    existing.getAffectedContractTransactions().size(),
                    payload.getAffectedContractTransactions().size()),
                existing.getAffectedContractTransactions().entrySet().stream()
                    .allMatch(
                        e ->
                            e.getValue()
                                .equals(payload.getAffectedContractTransactions().get(e.getKey()))))
            .allMatch(p -> p);

    if (!txMatches) {
      throw new RuntimeException("Invalid existing transaction");
    }

    // check if the box already exists
    // this is the easiest way to tell if a recipient has already been included
    for (RecipientBox existingBox : existing.getRecipientBoxes()) {
      if (Objects.equals(existingBox, encodedPayload.getRecipientBoxes().get(0))) {
        // recipient must already exist, so just act as though things went normally
        LOGGER.info("Recipient already existed in payload with hash {}", transactionHash);
        return transactionHash;
      }
    }

    final EncodedPayload.Builder existingPayloadBuilder = EncodedPayload.Builder.from(existing);

    // Boxes are all handled the same way. The new payload will only contain one box, and this will
    // be prepended
    // to the the list of existing boxes.
    final List<RecipientBox> existingBoxes = new ArrayList<>(existing.getRecipientBoxes());
    existingBoxes.add(0, encodedPayload.getRecipientBoxes().get(0));
    existingPayloadBuilder.withRecipientBoxes(
        existingBoxes.stream().map(RecipientBox::getData).collect(Collectors.toList()));

    // The case where a legacy transaction, which contains no recipients, is sent to us
    // is handled implicitly, as we don't need to add anything to the recipients list
    if (PrivacyMode.PRIVATE_STATE_VALIDATION == encodedPayload.getPrivacyMode()) {
      // PSV transaction, we have one box, and the relevant key is the first value
      // the existing payload will contain the key, but we can remove it and prepend the key again,
      // along with the
      // box
      final List<PublicKey> existingKeys = new ArrayList<>(existing.getRecipientKeys());
      final PublicKey newRecipient = encodedPayload.getRecipientKeys().get(0);
      if (!existingKeys.contains(newRecipient)) {
        throw new RuntimeException("expected recipient not found");
      }
      existingKeys.remove(newRecipient);
      existingKeys.add(0, newRecipient);
      existingPayloadBuilder.withNewRecipientKeys(existingKeys);
    } else if (!encodedPayload.getRecipientKeys().isEmpty()) {
      // Regular tx, add the recipient and the box
      final List<PublicKey> existingKeys = new ArrayList<>(existing.getRecipientKeys());
      final PublicKey newRecipient = encodedPayload.getRecipientKeys().get(0);

      existingKeys.add(0, newRecipient);
      existingPayloadBuilder.withNewRecipientKeys(existingKeys);
    }

    encryptedTransaction.setPayload(existingPayloadBuilder.build());
    this.encryptedTransactionDAO.update(encryptedTransaction);

    LOGGER.info("Updated existing payload with hash {}", transactionHash);
    return transactionHash;
  }

  @Override
  public void delete(MessageHash messageHash) {
    LOGGER.info("Received request to delete message with hash {}", messageHash);
    this.encryptedTransactionDAO.delete(messageHash);
  }

  @Override
  public void deleteAll(PublicKey publicKey) {
    LOGGER.info("Received request to delete message with hash {}", publicKey);
    this.encryptedTransactionDAO.deleteAll(publicKey);
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

      final PublicKey senderKey = PublicKey.from(encryptedRawTransaction.getSender());

      final RawTransaction rawTransaction =
          new RawTransaction(
              encryptedRawTransaction.getEncryptedPayload(),
              encryptedRawTransaction.getEncryptedKey(),
              new Nonce(encryptedRawTransaction.getNonce()),
              senderKey);

      final byte[] response = enclave.unencryptRawPayload(rawTransaction);
      return ReceiveResponse.Builder.create()
          .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
          .withUnencryptedTransactionData(response)
          .withManagedParties(Set.of(senderKey))
          .withSender(senderKey)
          .build();
    }

    final EncryptedTransaction encryptedTransaction =
        encryptedTransactionDAO
            .retrieveByHash(hash)
            .orElseThrow(
                () ->
                    new TransactionNotFoundException(
                        "Message with hash " + hash + " was not found"));

    final EncodedPayload payload =
        Optional.of(encryptedTransaction)
            .map(EncryptedTransaction::getPayload)
            .orElseThrow(
                () -> new IllegalStateException("Unable to decode previously encoded payload"));

    PublicKey recipientKey =
        request
            .getRecipient()
            .orElse(
                searchForRecipientKey(payload)
                    .orElseThrow(
                        () ->
                            new RecipientKeyNotFoundException(
                                "No suitable recipient keys found to decrypt payload for : "
                                    + hash)));

    byte[] unencryptedTransactionData = enclave.unencryptTransaction(payload, recipientKey);

    Set<MessageHash> affectedTransactions =
        payload.getAffectedContractTransactions().keySet().stream()
            .map(TxHash::getBytes)
            .map(MessageHash::new)
            .collect(Collectors.toSet());

    Set<PublicKey> managedParties = new HashSet<>();
    if (payload.getRecipientKeys().isEmpty()) {
      // legacy tx
      for (RecipientBox box : payload.getRecipientBoxes()) {
        EncodedPayload singleBoxPayload =
            EncodedPayload.Builder.from(payload).withRecipientBoxes(List.of(box.getData())).build();
        Optional<PublicKey> possibleRecipient = searchForRecipientKey(singleBoxPayload);
        possibleRecipient.ifPresent(managedParties::add);
      }
    } else {
      managedParties =
          enclave.getPublicKeys().stream()
              .filter(payload.getRecipientKeys()::contains)
              .collect(Collectors.toSet());
    }

    final ReceiveResponse.Builder responseBuilder = ReceiveResponse.Builder.create();
    payload.getPrivacyGroupId().ifPresent(responseBuilder::withPrivacyGroupId);

    return responseBuilder
        .withUnencryptedTransactionData(unencryptedTransactionData)
        .withPrivacyMode(payload.getPrivacyMode())
        .withAffectedTransactions(affectedTransactions)
        .withExecHash(payload.getExecHash())
        .withManagedParties(managedParties)
        .withSender(payload.getSenderKey())
        .build();
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
        enclave.encryptRawPayload(storeRequest.getPayload(), storeRequest.getSender());
    MessageHash hash = new MessageHash(payloadDigest.digest(rawTransaction.getEncryptedPayload()));
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
    return encryptedRawTransactionDAO.upcheck() && encryptedTransactionDAO.upcheck();
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
  public Set<PublicKey> getMandatoryRecipients(MessageHash transactionHash) {
    final EncodedPayload payload = this.fetchPayload(transactionHash);
    if (payload.getPrivacyMode() != PrivacyMode.MANDATORY_RECIPIENTS) {
      throw new MandatoryRecipientsNotAvailableException(
          "Operation invalid. Transaction found is not a mandatory recipients privacy type");
    }
    return payload.getMandatoryRecipients();
  }

  @Override
  public PublicKey defaultPublicKey() {
    return enclave.defaultPublicKey();
  }

  private EncodedPayload fetchPayload(final MessageHash hash) {
    return encryptedTransactionDAO
        .retrieveByHash(hash)
        .map(EncryptedTransaction::getPayload)
        .orElseThrow(
            () ->
                new TransactionNotFoundException(
                    "Message with hash "
                        + Base64.getEncoder().encodeToString(hash.getHashBytes())
                        + " was not found"));
  }
}
