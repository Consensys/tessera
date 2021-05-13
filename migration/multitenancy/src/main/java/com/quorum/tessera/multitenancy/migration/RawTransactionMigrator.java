package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.data.EncryptedRawTransaction;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class RawTransactionMigrator {

  private final EncryptedRawTransactionDAO primary;

  private final EncryptedRawTransactionDAO secondary;

  private final int maxBatchSize = 100;

  public RawTransactionMigrator(
      final EncryptedRawTransactionDAO primary, final EncryptedRawTransactionDAO secondary) {
    this.primary = Objects.requireNonNull(primary);
    this.secondary = Objects.requireNonNull(secondary);
  }

  public void migrate() {
    final long secondaryTxCount = secondary.transactionCount();
    final int batchCount = calculateBatchCount(maxBatchSize, secondaryTxCount);

    IntStream.range(0, batchCount)
        .map(i -> i * maxBatchSize)
        .mapToObj(offset -> secondary.retrieveTransactions(offset, maxBatchSize))
        .flatMap(List::stream)
        .forEach(
            ert -> {
              final Optional<EncryptedRawTransaction> existing =
                  primary.retrieveByHash(ert.getHash());
              if (existing.isEmpty()) {
                primary.save(ert);
              }
            });
  }

  private static int calculateBatchCount(final long maxResults, final long total) {
    return (int) Math.ceil((double) total / maxResults);
  }
}
