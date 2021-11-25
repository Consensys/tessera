package com.quorum.tessera.multitenancy.migration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Objects;

public class MigrationRunner {

  private final EntityManagerFactory primary;

  private final EntityManagerFactory secondary;

  public MigrationRunner(final EntityManagerFactory primary, final EntityManagerFactory secondary) {
    this.primary = Objects.requireNonNull(primary);
    this.secondary = Objects.requireNonNull(secondary);
  }

  public void run() {

    final EntityManager primaryEntityManager = primary.createEntityManager();
    final EntityManager secondaryEntityManager = secondary.createEntityManager();
    // migrate raw
    final RawTransactionMigrator rawMigrator =
        new RawTransactionMigrator(primaryEntityManager, secondaryEntityManager);
    rawMigrator.migrate();

    // migrate regular
    final EncryptedTransactionMigrator etMigrator =
        new EncryptedTransactionMigrator(primaryEntityManager, secondaryEntityManager);
    etMigrator.migrate();
  }
}
