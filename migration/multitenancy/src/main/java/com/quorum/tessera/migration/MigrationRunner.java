package com.quorum.tessera.migration;

import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.enclave.PayloadEncoder;

import java.util.Objects;

public class MigrationRunner {

    private final EntityManagerDAOFactory primary;

    private final EntityManagerDAOFactory secondary;

    public MigrationRunner(final EntityManagerDAOFactory primary, final EntityManagerDAOFactory secondary) {
        this.primary = Objects.requireNonNull(primary);
        this.secondary = Objects.requireNonNull(secondary);
    }

    public void run() {
        // migrate raw
        final RawTransactionMigrator rawMigrator =
                new RawTransactionMigrator(
                        primary.createEncryptedRawTransactionDAO(), secondary.createEncryptedRawTransactionDAO());
        rawMigrator.migrate();

        // migrate regular
        final EncryptedTransactionMigrator etMigrator =
                new EncryptedTransactionMigrator(
                        primary.createEncryptedTransactionDAO(),
                        secondary.createEncryptedTransactionDAO(),
                        PayloadEncoder.create());
        etMigrator.migrate();
    }
}
