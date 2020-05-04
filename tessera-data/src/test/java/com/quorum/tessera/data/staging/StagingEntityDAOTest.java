package com.quorum.tessera.data.staging;


import com.quorum.tessera.data.TestConfig;
import com.quorum.tessera.data.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;



@RunWith(Parameterized.class)
public class StagingEntityDAOTest {

    private EntityManagerFactory entityManagerFactory;

    private StagingEntityDAO stagingEntityDAO;

    private Map<String,StagingTransaction> transactions;

    private TestConfig testConfig;

    public StagingEntityDAOTest(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Before
    public void init() throws Exception {

        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url", testConfig.getUrl());
        properties.put("javax.persistence.jdbc.user","junit");
        properties.put("javax.persistence.jdbc.password","");
        properties.put("eclipselink.logging.logger","org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        properties.put("eclipselink.logging.level","FINE");
        properties.put("eclipselink.logging.parameters","true");
        properties.put("eclipselink.logging.level.sql","FINE");
        properties.put("javax.persistence.schema-generation.database.action","drop-and-create");
        properties.put("eclipselink.cache.shared.default","false");


        entityManagerFactory = Persistence.createEntityManagerFactory("tessera-recover",properties);

        stagingEntityDAO = new StagingEntityDAOImpl(entityManagerFactory);

        transactions = createFixtures();

    }

    @After
    public void clear() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from StagingTransactionVersion").executeUpdate();
        entityManager.createQuery("delete from StagingAffectedContractTransaction").executeUpdate();
        entityManager.createQuery("delete from StagingTransactionRecipient").executeUpdate();
        entityManager.createQuery("delete from StagingTransaction").executeUpdate();
        entityManager.getTransaction().commit();
        transactions.clear();
    }

    @Test
    public void saveDoesntAllowNullCipherText() {

        MessageHashStr messageHash = Utils.createHashStr();

        StagingTransaction stTransaction = new StagingTransaction();
        stTransaction.setHash(messageHash);

        try {
            stagingEntityDAO.save(stTransaction);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        } catch (PersistenceException ex) {

            String expectedMessage = String.format(testConfig.getRequiredFieldColumTemplate(),"CIPHER_TEXT");
            assertThat(ex)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining(expectedMessage)
                .hasMessageContaining("CIPHER_TEXT");
        }
    }

    @Test
    public void testStagingQuery() {


        final List<StagingTransaction> transactionsBeforeStaging =
            stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

        assertThat(transactionsBeforeStaging)
            .hasSize(6)
            .allMatch(stagingTransaction -> Objects.isNull(stagingTransaction.getValidationStage()));


        stagingEntityDAO.performStaging(Integer.MAX_VALUE);

        final List<StagingTransaction> verifiedTransactions = stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

        assertThat(verifiedTransactions).hasSize(6);

        assertThat(verifiedTransactions.get(0).getValidationStage()).isEqualTo(1L);
        assertThat(verifiedTransactions.get(1).getValidationStage()).isEqualTo(2L);
        assertThat(verifiedTransactions.get(2).getValidationStage()).isEqualTo(2L);
        assertThat(verifiedTransactions.get(3).getValidationStage()).isEqualTo(3L);
        assertThat(verifiedTransactions.get(4).getValidationStage()).isEqualTo(4L);

        assertThat(verifiedTransactions.get(5).getValidationStage()).isNull();

        final List<StagingTransaction> allTransactions =
            stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);
        // Transactions 1-4 and 7 are successfully resolved/staged. TXN5 is not - as it depends on an unknown
        // transaction - TXN6.

        assertThat(allTransactions.stream().filter(et -> et.getValidationStage() == null).count()).isEqualTo(1);
        assertThat(stagingEntityDAO.countAll()).isEqualTo(6);
        assertThat(stagingEntityDAO.countStaged()).isEqualTo(5);
    }

    @Test
    public void testRetrieveTransactionByHash() {

        final MessageHashStr txnHash7 = transactions.get("TXN7").getHash();
        final Optional<StagingTransaction> stagingTransaction = stagingEntityDAO.retrieveByHash(txnHash7);

        assertThat(stagingTransaction).isPresent();
        assertThat(stagingTransaction.get().getAffectedContractTransactions()).hasSize(2);
    }


    @Test
    public void testUpdate() {

            final MessageHashStr txnHash7 = transactions.get("TXN7").getHash();
            final Optional<StagingTransaction> stagingTransaction = stagingEntityDAO.retrieveByHash(txnHash7);

            assertThat(stagingTransaction).isPresent();
            StagingTransaction st = stagingTransaction.get();
            st.setValidationStage(123L);
            stagingEntityDAO.update(st);

            final Optional<StagingTransaction> stagingTransactionAfterUpdate = stagingEntityDAO.retrieveByHash(txnHash7);
            assertThat(stagingTransactionAfterUpdate).isPresent();

            assertThat(stagingTransactionAfterUpdate.get().getValidationStage()).isEqualTo(123L);

    }


    public static void addTransactionRecipients(StagingTransaction stagingTransaction) {
        final StagingRecipient stRecipient1 = new StagingRecipient("RECIPIENT1".getBytes());
        final StagingTransactionRecipientId stTransactionRecipientId1 =
            new StagingTransactionRecipientId(stagingTransaction.getHash(), stRecipient1);
        final StagingTransactionRecipient stTransactionRecipient1 = new StagingTransactionRecipient();
        stTransactionRecipient1.setId(stTransactionRecipientId1);
        stTransactionRecipient1.setInitiator(false);
        stTransactionRecipient1.setBox("BOX1".getBytes());
        stTransactionRecipient1.setTransaction(stagingTransaction);

        stagingTransaction.getRecipients().put(stRecipient1, stTransactionRecipient1);

        final StagingRecipient stRecipient2 = new StagingRecipient("RECIPIENT2".getBytes());
        final StagingTransactionRecipientId stTransactionRecipientId2 =
            new StagingTransactionRecipientId(stagingTransaction.getHash(), stRecipient2);
        final StagingTransactionRecipient stTransactionRecipient2 = new StagingTransactionRecipient();
        stTransactionRecipient2.setId(stTransactionRecipientId2);
        stTransactionRecipient2.setInitiator(false);
        stTransactionRecipient2.setBox("BOX1".getBytes());
        stTransactionRecipient2.setTransaction(stagingTransaction);

        stagingTransaction.getRecipients().put(stRecipient2, stTransactionRecipient2);
    }

    public Map<String,StagingTransaction> createFixtures() {

        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        final MessageHashStr txnHash1 = Utils.createHashStr();

        final StagingTransaction stTransaction1 = new StagingTransaction();
        stTransaction1.setHash(txnHash1);
        stTransaction1.setCipherText("CIPHERTEXT".getBytes());
        stTransaction1.setCipherTextNonce("ONE".getBytes());
        stTransaction1.setRecipientNonce("ONE".getBytes());
        stTransaction1.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction1);

        // add two versions for this transaction with no issues
        for (StagingTransactionRecipient stagingTransactionRecipient : stTransaction1.getRecipients().values()) {
            StagingTransactionVersion stagingTransactionVersion = new StagingTransactionVersion();
            stagingTransactionVersion.setId(stagingTransactionRecipient.getId());
            stagingTransactionVersion.setPayload("PAYLOAD".getBytes());
            stagingTransactionVersion.setTransaction(stTransaction1);
            stTransaction1
                .getVersions()
                .put(stagingTransactionVersion.getId().getRecipient(), stagingTransactionVersion);
        }


        entityManager.persist(stTransaction1);

        final MessageHashStr txnHash2 = Utils.createHashStr();

        final StagingTransaction stTransaction2 = new StagingTransaction();
        stTransaction2.setHash(txnHash2);
        stTransaction2.setCipherText("CIPHERTEXT".getBytes());
        stTransaction2.setCipherTextNonce("ONE".getBytes());
        stTransaction2.setRecipientNonce("ONE".getBytes());
        stTransaction2.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction2);

        StagingAffectedContractTransactionId stAffectedContractTransactionId21 =
            new StagingAffectedContractTransactionId(txnHash2, txnHash1);
        StagingAffectedContractTransaction stAffectedContractTransaction21 = new StagingAffectedContractTransaction();
        stAffectedContractTransaction21.setId(stAffectedContractTransactionId21);
        stAffectedContractTransaction21.setSecurityHash("SecureHash".getBytes());
        stAffectedContractTransaction21.setSourceTransaction(stTransaction2);

        stTransaction2.getAffectedContractTransactions().put(txnHash1, stAffectedContractTransaction21);

        entityManager.persist(stTransaction2);

        final MessageHashStr txnHash4 = Utils.createHashStr();
        // we are storing a transaction TXN4 which depends on another transaction TXN3 (which has not been received yet)
        final MessageHashStr txnHash3 = Utils.createHashStr();

        final StagingTransaction stTransaction4 = new StagingTransaction();
        stTransaction4.setHash(txnHash4);
        stTransaction4.setCipherText("CIPHERTEXT".getBytes());
        stTransaction4.setCipherTextNonce("ONE".getBytes());
        stTransaction4.setRecipientNonce("ONE".getBytes());
        stTransaction4.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction4);

        StagingAffectedContractTransactionId stAffectedContractTransactionId43 =
            new StagingAffectedContractTransactionId(txnHash4, txnHash3);
        StagingAffectedContractTransaction stAffectedContractTransaction43 = new StagingAffectedContractTransaction();
        stAffectedContractTransaction43.setId(stAffectedContractTransactionId43);
        stAffectedContractTransaction43.setSecurityHash("SecureHash".getBytes());
        stAffectedContractTransaction43.setSourceTransaction(stTransaction4);

        stTransaction4.getAffectedContractTransactions().put(txnHash3, stAffectedContractTransaction43);

        entityManager.persist(stTransaction4);

        final StagingTransaction stTransaction3 = new StagingTransaction();
        stTransaction3.setHash(txnHash3);
        stTransaction3.setCipherText("CIPHERTEXT".getBytes());
        stTransaction3.setCipherTextNonce("ONE".getBytes());
        stTransaction3.setRecipientNonce("ONE".getBytes());
        stTransaction3.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction3);

        StagingAffectedContractTransactionId stAffectedContractTransactionId31 =
            new StagingAffectedContractTransactionId(txnHash3, txnHash1);
        StagingAffectedContractTransaction stAffectedContractTransaction31 = new StagingAffectedContractTransaction();
        stAffectedContractTransaction31.setId(stAffectedContractTransactionId31);
        stAffectedContractTransaction31.setSecurityHash("SecureHash".getBytes());
        stAffectedContractTransaction31.setSourceTransaction(stTransaction3);

        stTransaction3.getAffectedContractTransactions().put(txnHash1, stAffectedContractTransaction31);

        entityManager.persist(stTransaction3);

        final MessageHashStr txnHash5 = Utils.createHashStr();
        // TXN5 is a unresolvable transaction as it depends on TXN6 which is never received
        final MessageHashStr txnHash6 = Utils.createHashStr();

        final StagingTransaction stTransaction5 = new StagingTransaction();
        stTransaction5.setHash(txnHash5);
        stTransaction5.setCipherText("CIPHERTEXT".getBytes());
        stTransaction5.setCipherTextNonce("ONE".getBytes());
        stTransaction5.setRecipientNonce("ONE".getBytes());
        stTransaction5.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction5);

        StagingAffectedContractTransactionId stAffectedContractTransactionId56 =
            new StagingAffectedContractTransactionId(txnHash5, txnHash6);
        StagingAffectedContractTransaction stAffectedContractTransaction56 = new StagingAffectedContractTransaction();
        stAffectedContractTransaction56.setId(stAffectedContractTransactionId56);
        stAffectedContractTransaction56.setSecurityHash("SecureHash".getBytes());
        stAffectedContractTransaction56.setSourceTransaction(stTransaction5);

        stTransaction5.getAffectedContractTransactions().put(txnHash6, stAffectedContractTransaction56);

        entityManager.persist(stTransaction5);

        final MessageHashStr txnHash7 = Utils.createHashStr();
        // TXN7 depends on TXN1 and TXN3
        final StagingTransaction stTransaction7 = new StagingTransaction();
        stTransaction7.setHash(txnHash7);
        stTransaction7.setCipherText("CIPHERTEXT".getBytes());
        stTransaction7.setCipherTextNonce("ONE".getBytes());
        stTransaction7.setRecipientNonce("ONE".getBytes());
        stTransaction7.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction7);

        StagingAffectedContractTransactionId stAffectedContractTransactionId71 =
            new StagingAffectedContractTransactionId(txnHash7, txnHash1);
        StagingAffectedContractTransaction stAffectedContractTransaction71 = new StagingAffectedContractTransaction();
        stAffectedContractTransaction71.setId(stAffectedContractTransactionId71);
        stAffectedContractTransaction71.setSecurityHash("SecureHash".getBytes());
        stAffectedContractTransaction71.setSourceTransaction(stTransaction7);

        stTransaction7.getAffectedContractTransactions().put(txnHash1, stAffectedContractTransaction71);

        StagingAffectedContractTransactionId stAffectedContractTransactionId74 =
            new StagingAffectedContractTransactionId(txnHash7, txnHash4);
        StagingAffectedContractTransaction stAffectedContractTransaction74 = new StagingAffectedContractTransaction();
        stAffectedContractTransaction74.setId(stAffectedContractTransactionId74);
        stAffectedContractTransaction74.setSecurityHash("SecureHash".getBytes());
        stAffectedContractTransaction74.setSourceTransaction(stTransaction7);

        stTransaction7.getAffectedContractTransactions().put(txnHash4, stAffectedContractTransaction74);

        entityManager.persist(stTransaction7);

        entityManager.getTransaction().commit();

        Map<String,StagingTransaction> transactions = new HashMap<>();
        transactions.put("TXN1",stTransaction1);
        transactions.put("TXN2",stTransaction2);
        transactions.put("TXN3",stTransaction3);
        transactions.put("TXN4",stTransaction4);
        transactions.put("TXN5",stTransaction5);
        transactions.put("TXN7",stTransaction7);
        return transactions;

    }

    @Parameterized.Parameters(name = "DB {0}")
    public static Collection<TestConfig> connectionDetails() {
        return List.of(TestConfig.values());
    }
}
