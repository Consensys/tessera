package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.data.EncryptedRawTransaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class RawTransactionMigrator {

  private final EntityManager primaryEntityManager;

  private final EntityManager secondaryEntityManager;

  private final int maxBatchSize = 100;

  public RawTransactionMigrator(
      final EntityManager primaryEntityManager, final EntityManager secondaryEntityManager) {
    this.primaryEntityManager = Objects.requireNonNull(primaryEntityManager);
    this.secondaryEntityManager = Objects.requireNonNull(secondaryEntityManager);
  }

  public void migrate() {

    final long secondaryTxCount =
        secondaryEntityManager
            .createQuery("select count(e) from EncryptedRawTransaction e", Long.class)
            .getSingleResult();
    final int batchCount = calculateBatchCount(maxBatchSize, secondaryTxCount);

    IntStream.range(0, batchCount)
        .map(i -> i * maxBatchSize)
        .mapToObj(
            offset ->
                secondaryEntityManager
                    .createNamedQuery(
                        "EncryptedRawTransaction.FindAll", EncryptedRawTransaction.class)
                    .setFirstResult(offset)
                    .setMaxResults(maxBatchSize))
        .flatMap(TypedQuery::getResultStream)
        .forEach(
            ert -> {
              final Optional<EncryptedRawTransaction> existing =
                  Optional.ofNullable(
                      primaryEntityManager.find(EncryptedRawTransaction.class, ert.getHash()));
              if (existing.isEmpty()) {
                primaryEntityManager.getTransaction().begin();
                primaryEntityManager.persist(ert);
                primaryEntityManager.getTransaction().commit();
              }
            });
  }

  private static int calculateBatchCount(final long maxResults, final long total) {
    return (int) Math.ceil((double) total / maxResults);
  }
}
