package com.quorum.tessera.data.staging;


import com.quorum.tessera.data.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

//TODO: Rewite tests in a maintainable way
@RunWith(Parameterized.class)
public class StagingEntityDAOTest {

    private StagingEntityDAO stagingEntityDAO;

    private EntityManagerFactory entityManagerFactory;

    private TestConfig testConfig;

    public StagingEntityDAOTest(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Before
    public void onSetUp() {

        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url",testConfig.getUrl());
        properties.put("javax.persistence.jdbc.user","junit");
        properties.put("javax.persistence.jdbc.password","");
        properties.put("eclipselink.logging.logger","org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        properties.put("eclipselink.logging.level","FINE");
        properties.put("eclipselink.logging.parameters","true");
        properties.put("eclipselink.logging.level.sql","FINE");
        properties.put("javax.persistence.schema-generation.database.action","drop-and-create");

        entityManagerFactory = Persistence.createEntityManagerFactory("tessera-recover",properties);

        stagingEntityDAO = new StagingEntityDAOImpl(entityManagerFactory);

    }

    @After
    public void onTearDown() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from StagingTransactionVersion").executeUpdate();
        entityManager.createQuery("delete from StagingAffectedContractTransaction").executeUpdate();
        entityManager.createQuery("delete from StagingTransactionRecipient").executeUpdate();
        entityManager.createQuery("delete from StagingTransaction").executeUpdate();
        entityManager.getTransaction().commit();
    }



    @Test
    public void saveDoesntAllowNullCipherText() {

        MessageHashStr messageHashStr = new MessageHashStr(UUID.randomUUID().toString().getBytes());

        StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setHash(messageHashStr);

        try {
            stagingEntityDAO.save(stagingTransaction);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        } catch (PersistenceException ex) {
            String expectedMessage = String.format(testConfig.getRequiredFieldColumTemplate(),"CIPHER_TEXT");
            assertThat(ex)
                .hasMessageContaining(expectedMessage).hasMessageContaining("CIPHER_TEXT");
        }

    }

    @Ignore
    @Test
    public void testStagingQuery() {

        final List<StagingTransaction> transactionsBeforeStaging =
            stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

        transactionsBeforeStaging.forEach(
            stagingTransaction -> {
                assertThat(stagingTransaction.getValidationStage()).isNull();
            });

        stagingEntityDAO.performStaging(Integer.MAX_VALUE);

        final List<StagingTransaction> verifiedTransactions =
            stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);
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

    @Ignore
    @Test
    public void testRetrieveTransactionByHash() {
        final MessageHashStr messageHash = new MessageHashStr(UUID.randomUUID().toString().getBytes());
        final StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setHash(messageHash);
        stagingTransaction.setCipherText("CipherText".getBytes());
        stagingTransaction.setCipherTextNonce("CipherTextNonce".getBytes());
        stagingTransaction.setSenderKey("SenderKey".getBytes());
        stagingTransaction.setRecipientNonce("RecipientNonce".getBytes());

        Map<MessageHashStr,StagingAffectedContractTransaction> stagingAffectedContractTransactions =
            IntStream.range(0,2)
                .mapToObj(i -> {
                    StagingAffectedContractTransaction txn = new StagingAffectedContractTransaction();
                    txn.setId(new StagingAffectedContractTransactionId(messageHash,new MessageHashStr(UUID.randomUUID().toString().getBytes())));
                    txn.setSourceTransaction(stagingTransaction);
                    return txn;
                }).collect(Collectors.toMap(t -> t.getSourceTransaction().getHash(), t -> t));

        //stagingTransaction.setAffectedContractTransactions(stagingAffectedContractTransactions);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(stagingTransaction);
        entityManager.getTransaction().commit();

        final Optional<StagingTransaction> result = stagingEntityDAO.retrieveByHash(messageHash);

        assertThat(result).isPresent();
        assertThat(result.get().getAffectedContractTransactions()).hasSize(2);
    }

    @Test
    public void testDeleteTransactionByHash() {

        final MessageHashStr messageHash = new MessageHashStr(UUID.randomUUID().toString().getBytes());
        final StagingTransaction stagingTransaction = new StagingTransaction();
        stagingTransaction.setHash(messageHash);
        stagingTransaction.setCipherText("CipherText".getBytes());
        stagingTransaction.setCipherTextNonce("CipherTextNonce".getBytes());
        stagingTransaction.setSenderKey("SenderKey".getBytes());
        stagingTransaction.setRecipientNonce("RecipientNonce".getBytes());

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query countQuery = entityManager.createQuery("select count(t) from StagingTransaction t",Long.class);

        entityManager.getTransaction().begin();
        entityManager.persist(stagingTransaction);
        entityManager.getTransaction().commit();

        assertThat(countQuery.getSingleResult()).isEqualTo(1L);

        stagingEntityDAO.delete(messageHash);

        long result = (long) countQuery.getSingleResult();
        assertThat(result).isZero();
    }

    @Ignore
    @Test
    public void testUpdate() {
        final MessageHashStr txnHash7 = new MessageHashStr("TXN7".getBytes());
        final Optional<StagingTransaction> stagingTransaction = stagingEntityDAO.retrieveByHash(txnHash7);

        assertThat(stagingTransaction).isPresent();
        StagingTransaction st = stagingTransaction.get();
        st.setValidationStage(123L);
        stagingEntityDAO.update(st);


        final Optional<StagingTransaction> stagingTransactionAfterUpdate = stagingEntityDAO.retrieveByHash(txnHash7);
        assertThat(stagingTransactionAfterUpdate).isPresent();

        assertThat(stagingTransactionAfterUpdate.get().getValidationStage()).isEqualTo(123L);
    }

    @Test
    public void testCleanupStagingData() {
        stagingEntityDAO.cleanStagingArea(Integer.MAX_VALUE);

        final List<StagingTransaction> transactions =
            stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);

        assertThat(transactions).isEmpty();
    }



    @Parameterized.Parameters(name = "DB {0}")
    public static Collection<TestConfig> connectionDetails() {
        return List.of(TestConfig.values());
    }


}
