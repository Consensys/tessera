package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.data.EncryptedRawTransaction;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.MessageHash;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class MigrationCliAdapterTest {

    @Test
    public void adapterType() {
        assertThat(new MigrationCliAdapter().getType()).isEqualTo(CliType.MULTITENANCY_MIGRATION);
    }

    @Test
    public void simpleMigration() {
        final EncryptedRawTransaction secondaryRawTx =
                new EncryptedRawTransaction(
                        new MessageHash("somehash".getBytes()),
                        "some encrypted message".getBytes(),
                        "encryptedKey".getBytes(),
                        "nonce".getBytes(),
                        "sender".getBytes());

        final EncryptedTransaction secondaryTx =
                new EncryptedTransaction(
                        new MessageHash("encryptedTransactionHash".getBytes()), "sampleencodedpayload".getBytes());

        final Config primaryConfig = new Config();
        final JdbcConfig jdbc1 = new JdbcConfig("sa", "", "jdbc:h2:mem:tessera1");
        jdbc1.setAutoCreateTables(true);
        primaryConfig.setJdbcConfig(jdbc1);

        final Config secondaryConfig = new Config();
        final JdbcConfig jdbc2 = new JdbcConfig("sa", "", "jdbc:h2:mem:tessera2");
        jdbc2.setAutoCreateTables(true);
        secondaryConfig.setJdbcConfig(jdbc2);

        final EntityManagerDAOFactory emf2 = EntityManagerDAOFactory.newFactory(secondaryConfig);
        emf2.createEncryptedRawTransactionDAO().save(secondaryRawTx);
        emf2.createEncryptedTransactionDAO().save(secondaryTx);

        final MigrationCliAdapter adapter = new MigrationCliAdapter();
        adapter.configPrimary = primaryConfig;
        adapter.configSecondary = secondaryConfig;
        adapter.call();

        // check the transactions are present in the primary database
        final EntityManagerDAOFactory emf1 = EntityManagerDAOFactory.newFactory(primaryConfig);
        final Optional<EncryptedRawTransaction> newlySavedErt =
                emf1.createEncryptedRawTransactionDAO().retrieveByHash(new MessageHash("somehash".getBytes()));
        assertThat(newlySavedErt).isPresent();
        assertThat(newlySavedErt.get().getEncryptedKey()).isEqualTo(secondaryRawTx.getEncryptedKey());
        assertThat(newlySavedErt.get().getEncryptedPayload()).isEqualTo(secondaryRawTx.getEncryptedPayload());
        assertThat(newlySavedErt.get().getNonce()).isEqualTo(secondaryRawTx.getNonce());
        assertThat(newlySavedErt.get().getSender()).isEqualTo(secondaryRawTx.getSender());
        assertThat(newlySavedErt.get().getHash()).isEqualTo(secondaryRawTx.getHash());

        final Optional<EncryptedTransaction> newlySavedEt =
                emf1.createEncryptedTransactionDAO()
                        .retrieveByHash(new MessageHash("encryptedTransactionHash".getBytes()));
        assertThat(newlySavedEt).isPresent();
        assertThat(newlySavedEt.get().getHash()).isEqualTo(secondaryTx.getHash());
        assertThat(newlySavedEt.get().getEncodedPayload()).isEqualTo(secondaryTx.getEncodedPayload());
    }
}
