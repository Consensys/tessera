package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.enclave.SecurityHash;
import com.quorum.tessera.enclave.TxHash;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EncryptedTransactionMigrator {

  private final EntityManager primaryEntityManager;

  private final EntityManager secondaryEntityManager;

  private final int maxBatchSize = 100;

  public EncryptedTransactionMigrator(
      final EntityManager primaryEntityManager, final EntityManager secondaryEntityManager) {
    this.primaryEntityManager = Objects.requireNonNull(primaryEntityManager);
    this.secondaryEntityManager = Objects.requireNonNull(secondaryEntityManager);
  }

  public void migrate() {

    final long secondaryTxCount =
        secondaryEntityManager
            .createQuery("select count(e) from EncryptedTransaction e", Long.class)
            .getSingleResult();
    final int batchCount = calculateBatchCount(maxBatchSize, secondaryTxCount);

    IntStream.range(0, batchCount)
        .map(i -> i * maxBatchSize)
        .mapToObj(
            offset ->
                secondaryEntityManager
                    .createNamedQuery("EncryptedTransaction.FindAll", EncryptedTransaction.class)
                    .setFirstResult(offset)
                    .setMaxResults(maxBatchSize))
        .flatMap(TypedQuery::getResultStream)
        .forEach(
            et -> {
              final Optional<EncryptedTransaction> existing =
                  primaryEntityManager
                      .createNamedQuery(
                          "EncryptedTransaction.FindByHash", EncryptedTransaction.class)
                      .setParameter("hash", et.getHash().getHashBytes())
                      .getResultStream()
                      .findAny();

              if (existing.isEmpty()) {
                primaryEntityManager.getTransaction().begin();
                primaryEntityManager.persist(et);
                primaryEntityManager.getTransaction().commit();
                return;
              }

              final EncryptedTransaction outerTx = existing.get();

              final EncodedPayload primaryTx = outerTx.getPayload();
              final EncodedPayload secondaryTx = et.getPayload();

              final EncodedPayload updatedPayload =
                  this.handleSingleTransaction(primaryTx, secondaryTx);

              outerTx.setPayload(updatedPayload);
              primaryEntityManager.getTransaction().begin();
              primaryEntityManager.merge(outerTx);
              primaryEntityManager.getTransaction().commit();
            });
  }

  public EncodedPayload handleSingleTransaction(
      final EncodedPayload primaryTx, final EncodedPayload secondaryTx) {
    if (primaryTx.getPrivacyMode() == PrivacyMode.PRIVATE_STATE_VALIDATION) {
      return this.migratePsvTx(primaryTx, secondaryTx);
    }

    if (primaryTx.getPrivacyMode() == PrivacyMode.PARTY_PROTECTION) {
      return this.migratePpTx(primaryTx, secondaryTx);
    }

    return this.migrateSpTx(primaryTx, secondaryTx);
  }

  private EncodedPayload migratePsvTx(
      final EncodedPayload primaryTx, final EncodedPayload secondaryTx) {
    if (primaryTx.getRecipientBoxes().size() == primaryTx.getRecipientKeys().size()) {
      return primaryTx;
    }

    if (secondaryTx.getRecipientBoxes().size() == secondaryTx.getRecipientKeys().size()) {
      return secondaryTx;
    }

    final List<PublicKey> recipients = new ArrayList<>(primaryTx.getRecipientKeys());
    // remove the public keys from secondary
    final List<PublicKey> secondaryRecipients =
        secondaryTx.getRecipientKeys().subList(0, secondaryTx.getRecipientBoxes().size());
    recipients.removeAll(secondaryRecipients);
    recipients.addAll(0, secondaryRecipients);

    final List<RecipientBox> boxes = new ArrayList<>(primaryTx.getRecipientBoxes());
    boxes.addAll(0, secondaryTx.getRecipientBoxes());

    return EncodedPayload.Builder.from(primaryTx)
        .withAffectedContractTransactions(combineAcoths(primaryTx, secondaryTx))
        .withNewRecipientKeys(recipients)
        .withRecipientBoxes(boxes.stream().map(RecipientBox::getData).collect(Collectors.toList()))
        .build();
  }

  private EncodedPayload migratePpTx(
      final EncodedPayload primaryTx, final EncodedPayload secondaryTx) {
    if (primaryTx.getRecipientKeys().contains(primaryTx.getSenderKey())) {
      return primaryTx;
    }
    if (secondaryTx.getRecipientKeys().contains(secondaryTx.getSenderKey())) {
      return secondaryTx;
    }

    List<PublicKey> recipients = new ArrayList<>(primaryTx.getRecipientKeys());
    recipients.addAll(secondaryTx.getRecipientKeys());

    List<RecipientBox> boxes = new ArrayList<>(primaryTx.getRecipientBoxes());
    boxes.addAll(secondaryTx.getRecipientBoxes());

    return EncodedPayload.Builder.from(primaryTx)
        .withAffectedContractTransactions(combineAcoths(primaryTx, secondaryTx))
        .withNewRecipientKeys(recipients)
        .withRecipientBoxes(boxes.stream().map(RecipientBox::getData).collect(Collectors.toList()))
        .build();
  }

  private EncodedPayload migrateSpTx(
      final EncodedPayload primaryTx, final EncodedPayload secondaryTx) {
    if (primaryTx.getRecipientKeys().contains(primaryTx.getSenderKey())) {
      return primaryTx;
    }

    if (secondaryTx.getRecipientKeys().contains(secondaryTx.getSenderKey())) {
      return secondaryTx;
    }

    if (primaryTx.getRecipientKeys().isEmpty() && secondaryTx.getRecipientKeys().isEmpty()) {
      List<PublicKey> recipients = new ArrayList<>(primaryTx.getRecipientKeys());
      recipients.addAll(secondaryTx.getRecipientKeys());

      List<RecipientBox> boxes = new ArrayList<>(primaryTx.getRecipientBoxes());
      boxes.addAll(secondaryTx.getRecipientBoxes());

      return EncodedPayload.Builder.from(primaryTx)
          .withNewRecipientKeys(recipients)
          .withRecipientBoxes(
              boxes.stream().map(RecipientBox::getData).collect(Collectors.toList()))
          .build();
    }

    if (!primaryTx.getRecipientKeys().isEmpty() && !secondaryTx.getRecipientKeys().isEmpty()) {
      final List<PublicKey> recipients = new ArrayList<>(primaryTx.getRecipientKeys());
      recipients.addAll(secondaryTx.getRecipientKeys());

      final List<RecipientBox> boxes = new ArrayList<>(primaryTx.getRecipientBoxes());
      boxes.addAll(secondaryTx.getRecipientBoxes());

      return EncodedPayload.Builder.from(primaryTx)
          .withNewRecipientKeys(recipients)
          .withRecipientBoxes(
              boxes.stream().map(RecipientBox::getData).collect(Collectors.toList()))
          .build();
    }

    // primary was a pre-0.8 sender
    if (!primaryTx.getRecipientKeys().isEmpty()) {
      return primaryTx;
    }
    // only option left - secondary was a pre-0.8 sender
    return secondaryTx;
  }

  static Map<TxHash, byte[]> combineAcoths(
      final EncodedPayload primaryTx, final EncodedPayload secondaryTx) {
    final Map<TxHash, SecurityHash> combinedAffectedTxs =
        new HashMap<>(primaryTx.getAffectedContractTransactions());
    combinedAffectedTxs.putAll(secondaryTx.getAffectedContractTransactions());

    return combinedAffectedTxs.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().getData()));
  }

  static int calculateBatchCount(long maxResults, long total) {
    return (int) Math.ceil((double) total / maxResults);
  }
}
