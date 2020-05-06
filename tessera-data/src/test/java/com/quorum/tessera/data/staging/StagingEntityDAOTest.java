package com.quorum.tessera.data.staging;


import com.quorum.tessera.data.TestConfig;
import com.quorum.tessera.data.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
        properties.put("eclipselink.session.customizer","com.quorum.tessera.eclipselink.AtomicLongSequence");

        entityManagerFactory = Persistence.createEntityManagerFactory("tessera-recover",properties);

        stagingEntityDAO = new StagingEntityDAOImpl(entityManagerFactory);

        transactions = createFixtures();

    }

    @After
    public void clear() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from StagingTransactionVersion").executeUpdate();
        entityManager.createQuery("delete from StagingAffectedTransaction").executeUpdate();
        entityManager.createQuery("delete from StagingRecipient").executeUpdate();
        entityManager.createQuery("delete from StagingTransaction").executeUpdate();
        entityManager.getTransaction().commit();
        transactions.clear();
    }

    @Test
    public void saveDoesntAllowNullCipherText() {

        String messageHash = Base64.getEncoder().encodeToString(Utils.randomBytes());

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
    public void updateStageForBatch() {

        final long validationStage = new Random().nextLong();
        final int batchSize = 1;

        int results = stagingEntityDAO.updateStageForBatch(batchSize,validationStage);
        assertThat(results).isEqualTo(batchSize);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<StagingTransaction> root = criteriaQuery.from(StagingTransaction.class);
        criteriaQuery
            .select(criteriaBuilder.count(root))
            .where(
                criteriaBuilder.equal(root.get("validationStage"),validationStage)
            );

        Long countPending = entityManager.createQuery(criteriaQuery)
            .setParameter("stage",validationStage)
            .getSingleResult();

        assertThat(countPending).isEqualTo((long) batchSize);

    }

    @Test
    public void testRetrieveTransactionByHash() {

        final String txnHash7 = transactions.get("TXN7").getHash();
        final Optional<StagingTransaction> stagingTransaction = stagingEntityDAO.retrieveByHash(txnHash7);

        assertThat(stagingTransaction).isPresent();
        assertThat(stagingTransaction.get().getAffectedContractTransactions()).hasSize(2);
    }


    @Test
    public void testUpdate() {

            final String txnHash7 = transactions.get("TXN7").getHash();
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


        stRecipient1.setMessageHash(stagingTransaction.getHash());
        stRecipient1.setInitiator(false);
        stRecipient1.setBox("BOX1".getBytes());
        stRecipient1.setTransaction(stagingTransaction);

        stagingTransaction.getRecipients().add(stRecipient1);

        final StagingRecipient stRecipient2 = new StagingRecipient("RECIPIENT2".getBytes());

        stRecipient2.setMessageHash(stagingTransaction.getHash());
        stRecipient2.setInitiator(false);
        stRecipient2.setBox("BOX1".getBytes());
        stRecipient2.setTransaction(stagingTransaction);

        stagingTransaction.getRecipients().add(stRecipient2);
    }

    public Map<String,StagingTransaction> createFixtures() {

        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        final String txnHash1 = Utils.createHashStr();

        final StagingTransaction stTransaction1 = new StagingTransaction();
        stTransaction1.setHash(txnHash1);
        stTransaction1.setCipherText("CIPHERTEXT".getBytes());
        stTransaction1.setCipherTextNonce("ONE".getBytes());
        stTransaction1.setRecipientNonce("ONE".getBytes());
        stTransaction1.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction1);

        // add two versions for this transaction with no issues
        for (StagingRecipient stagingTransactionRecipient : stTransaction1.getRecipients()) {
            stagingTransactionRecipient.setMessageHash(stTransaction1.getHash());
            StagingTransactionVersion stagingTransactionVersion = new StagingTransactionVersion();
            stagingTransactionVersion.setPayload("PAYLOAD".getBytes());
            stagingTransactionVersion.setTransaction(stTransaction1);


            stTransaction1
                .getVersions()
                .add(stagingTransactionVersion);
        }


        entityManager.persist(stTransaction1);

        final String txnHash2 = Utils.createHashStr();

        final StagingTransaction stTransaction2 = new StagingTransaction();
        stTransaction2.setHash(txnHash2);
        stTransaction2.setCipherText("CIPHERTEXT".getBytes());
        stTransaction2.setCipherTextNonce("ONE".getBytes());
        stTransaction2.setRecipientNonce("ONE".getBytes());
        stTransaction2.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction2);

        StagingAffectedTransaction stAffectedContractTransaction21 = new StagingAffectedTransaction();
        stAffectedContractTransaction21.setHash(txnHash1);

        stAffectedContractTransaction21.setSourceTransaction(stTransaction2);

        stTransaction2.getAffectedContractTransactions().add(stAffectedContractTransaction21);

        entityManager.persist(stTransaction2);

        final String txnHash4 = Utils.createHashStr();
        // we are storing a transaction TXN4 which depends on another transaction TXN3 (which has not been received yet)
        final String txnHash3 = Utils.createHashStr();

        final StagingTransaction stTransaction4 = new StagingTransaction();
        stTransaction4.setHash(txnHash4);
        stTransaction4.setCipherText("CIPHERTEXT".getBytes());
        stTransaction4.setCipherTextNonce("ONE".getBytes());
        stTransaction4.setRecipientNonce("ONE".getBytes());
        stTransaction4.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction4);

        StagingAffectedTransaction stAffectedContractTransaction43 = new StagingAffectedTransaction();
        stAffectedContractTransaction43.setHash(txnHash3);
        stAffectedContractTransaction43.setSourceTransaction(stTransaction4);

        stTransaction4.getAffectedContractTransactions().add(stAffectedContractTransaction43);

        entityManager.persist(stTransaction4);

        final StagingTransaction stTransaction3 = new StagingTransaction();
        stTransaction3.setHash(txnHash3);
        stTransaction3.setCipherText("CIPHERTEXT".getBytes());
        stTransaction3.setCipherTextNonce("ONE".getBytes());
        stTransaction3.setRecipientNonce("ONE".getBytes());
        stTransaction3.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction3);

        StagingAffectedTransaction stAffectedContractTransaction31 = new StagingAffectedTransaction();
        stAffectedContractTransaction31.setHash(txnHash1);
        stAffectedContractTransaction31.setSourceTransaction(stTransaction3);

        stTransaction3.getAffectedContractTransactions().add(stAffectedContractTransaction31);

        entityManager.persist(stTransaction3);

        final String txnHash5 = Utils.createHashStr();
        // TXN5 is a unresolvable transaction as it depends on TXN6 which is never received
        final String txnHash6 = Utils.createHashStr();

        final StagingTransaction stTransaction5 = new StagingTransaction();
        stTransaction5.setHash(txnHash5);
        stTransaction5.setCipherText("CIPHERTEXT".getBytes());
        stTransaction5.setCipherTextNonce("ONE".getBytes());
        stTransaction5.setRecipientNonce("ONE".getBytes());
        stTransaction5.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction5);

        StagingAffectedTransaction stAffectedContractTransaction56 = new StagingAffectedTransaction();
        stAffectedContractTransaction56.setHash(txnHash6);
        stAffectedContractTransaction56.setSourceTransaction(stTransaction5);

        stTransaction5.getAffectedContractTransactions().add(stAffectedContractTransaction56);

        entityManager.persist(stTransaction5);

        final String txnHash7 = Utils.createHashStr();
        // TXN7 depends on TXN1 and TXN3
        final StagingTransaction stTransaction7 = new StagingTransaction();
        stTransaction7.setHash(txnHash7);
        stTransaction7.setCipherText("CIPHERTEXT".getBytes());
        stTransaction7.setCipherTextNonce("ONE".getBytes());
        stTransaction7.setRecipientNonce("ONE".getBytes());
        stTransaction7.setSenderKey("SENDER".getBytes());

        addTransactionRecipients(stTransaction7);

        StagingAffectedTransaction stAffectedContractTransaction71 = new StagingAffectedTransaction();
        stAffectedContractTransaction71.setHash(txnHash1);
        stAffectedContractTransaction71.setSourceTransaction(stTransaction7);

        stTransaction7.getAffectedContractTransactions().add(stAffectedContractTransaction71);

        StagingAffectedTransaction stAffectedContractTransaction74 = new StagingAffectedTransaction();
        stAffectedContractTransaction74.setHash(txnHash4);
        stAffectedContractTransaction74.setSourceTransaction(stTransaction7);

        stTransaction7.getAffectedContractTransactions().add(stAffectedContractTransaction74);

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
