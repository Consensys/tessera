package com.quorum.tessera.transaction.resend.internal;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.resend.ResendManager;
import java.util.*;

public class ResendManagerImpl implements ResendManager {

  private final EncryptedTransactionDAO encryptedTransactionDAO;

  private final Enclave enclave;

  private final PayloadDigest payloadDigest;

  public ResendManagerImpl(
      final EncryptedTransactionDAO dao, final Enclave enclave, final PayloadDigest payloadDigest) {
    this.encryptedTransactionDAO = dao;
    this.enclave = enclave;
    this.payloadDigest = payloadDigest;
  }

  // TODO: synchronize based on messagehash, so different message don't lock each other
  public synchronized void acceptOwnMessage(final EncodedPayload payload) {
    // check the payload can be decrypted to ensure it isn't rubbish being sent to us
    final byte[] newDecrypted;
    if (payload.getPrivacyMode() == PrivacyMode.PRIVATE_STATE_VALIDATION) {
      // if it is PSV, then the enclave would be expected our own box to be available,
      // but it isn't (since we are rebuilding)
      // since we only want to decrypt the tx and not worry about all the other pieces,
      // treat it just like a standard private tx, and remove the other recipients
      final EncodedPayload tempPayload =
          EncodedPayload.Builder.from(payload)
              .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
              .withExecHash(new byte[0])
              .withNewRecipientKeys(List.of(payload.getRecipientKeys().get(0)))
              .withRecipientBoxes(List.of(payload.getRecipientBoxes().get(0).getData()))
              .build();
      newDecrypted = enclave.unencryptTransaction(tempPayload, payload.getSenderKey());
    } else {
      newDecrypted = enclave.unencryptTransaction(payload, payload.getSenderKey());
    }

    final MessageHash transactionHash =
        Optional.of(payload)
            .map(EncodedPayload::getCipherText)
            .map(payloadDigest::digest)
            .map(MessageHash::new)
            .get();

    final PublicKey sender = payload.getSenderKey();
    if (!enclave.getPublicKeys().contains(sender)) {
      throw new IllegalArgumentException(
          "Message "
              + transactionHash.toString()
              + " does not have one the nodes own keys as a sender");
    }

    // this is a tx which we created
    final Optional<EncryptedTransaction> tx =
        this.encryptedTransactionDAO.retrieveByHash(transactionHash);

    if (tx.isPresent()) {

      // we just need to add the recipient
      final EncodedPayload existing = tx.get().getPayload();

      // check if the box already exists
      // this is the easiest way to tell if a recipient has already been included
      for (RecipientBox existingBox : existing.getRecipientBoxes()) {
        if (Objects.equals(existingBox, payload.getRecipientBoxes().get(0))) {
          // recipient must already exist, so just act as though things went normally
          return;
        }
      }

      final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.from(existing);

      if (!existing.getRecipientKeys().contains(payload.getRecipientKeys().get(0))) {
        // lets compare it against another message received before
        final byte[] oldDecrypted = enclave.unencryptTransaction(existing, existing.getSenderKey());
        final boolean same =
            Arrays.equals(newDecrypted, oldDecrypted)
                && Arrays.equals(payload.getCipherText(), existing.getCipherText());

        if (!same) {
          throw new IllegalArgumentException("Invalid payload provided");
        }

        // check recipients
        if (!existing.getRecipientKeys().contains(payload.getRecipientKeys().get(0))) {
          payloadBuilder
              .withRecipientKey(payload.getRecipientKeys().get(0))
              .withRecipientBox(payload.getRecipientBoxes().get(0).getData());
        }

        EncryptedTransaction encryptedTransaction = tx.get();

        encryptedTransaction.setPayload(payloadBuilder.build());

        this.encryptedTransactionDAO.update(encryptedTransaction);
      }

    } else {

      final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.from(payload);
      final List<PublicKey> recipientKeys = new ArrayList<>(payload.getRecipientKeys());

      // we need to recreate this
      if (!recipientKeys.contains(sender)) {
        recipientKeys.add(sender);
        payloadBuilder.withRecipientKey(sender);
      }

      // add recipient boxes for all recipients (applicable for PSV transactions)
      for (int i = payload.getRecipientBoxes().size(); i < recipientKeys.size(); i++) {
        PublicKey recipient = recipientKeys.get(i);
        byte[] newBox = enclave.createNewRecipientBox(payload, recipient);
        payloadBuilder.withRecipientBox(newBox);
      }

      final EncryptedTransaction txToSave =
          new EncryptedTransaction(transactionHash, payloadBuilder.build());

      this.encryptedTransactionDAO.save(txToSave);
    }
  }
}
