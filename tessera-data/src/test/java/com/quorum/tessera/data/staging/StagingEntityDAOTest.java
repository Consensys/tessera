//package com.quorum.tessera.data.staging;
//
//import com.quorum.tessera.data.jpatest.JpaH2Config;
//import com.quorum.tessera.data.jpatest.JpaHsqlConfig;
//import com.quorum.tessera.data.jpatest.JpaSqliteConfig;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Suite;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.TransactionDefinition;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.support.DefaultTransactionDefinition;
//
//import javax.inject.Inject;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.PersistenceException;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.catchThrowable;
//
//@RunWith(Suite.class)
//@Suite.SuiteClasses({
//    StagingEntityDAOTest.H2Test.class,
//    StagingEntityDAOTest.HsqlTest.class,
//    StagingEntityDAOTest.SqliteTest.class
//})
//public class StagingEntityDAOTest {
//
//    public static void addTransactionRecipients(StagingTransaction stagingTransaction) {
//        final StagingRecipient stRecipient1 = new StagingRecipient("RECIPIENT1".getBytes());
//        final StagingTransactionRecipientId stTransactionRecipientId1 =
//                new StagingTransactionRecipientId(stagingTransaction.getHash(), stRecipient1);
//        final StagingTransactionRecipient stTransactionRecipient1 = new StagingTransactionRecipient();
//        stTransactionRecipient1.setId(stTransactionRecipientId1);
//        stTransactionRecipient1.setInitiator(false);
//        stTransactionRecipient1.setBox("BOX1".getBytes());
//        stTransactionRecipient1.setTransaction(stagingTransaction);
//
//        stagingTransaction.getRecipients().put(stRecipient1, stTransactionRecipient1);
//
//        final StagingRecipient stRecipient2 = new StagingRecipient("RECIPIENT2".getBytes());
//        final StagingTransactionRecipientId stTransactionRecipientId2 =
//                new StagingTransactionRecipientId(stagingTransaction.getHash(), stRecipient2);
//        final StagingTransactionRecipient stTransactionRecipient2 = new StagingTransactionRecipient();
//        stTransactionRecipient2.setId(stTransactionRecipientId2);
//        stTransactionRecipient2.setInitiator(false);
//        stTransactionRecipient2.setBox("BOX1".getBytes());
//        stTransactionRecipient2.setTransaction(stagingTransaction);
//
//        stagingTransaction.getRecipients().put(stRecipient2, stTransactionRecipient2);
//    }
//
//    public static void initialiseTransactions(EntityManager entityManager, StagingEntityDAO stEntityDAO) {
//
//        final MessageHashStr txnHash1 = new MessageHashStr("TXN1".getBytes());
//
//        final StagingTransaction stTransaction1 = new StagingTransaction();
//        stTransaction1.setHash(txnHash1);
//        stTransaction1.setCipherText("CIPHERTEXT".getBytes());
//        stTransaction1.setCipherTextNonce("ONE".getBytes());
//        stTransaction1.setRecipientNonce("ONE".getBytes());
//        stTransaction1.setSenderKey("SENDER".getBytes());
//
//        addTransactionRecipients(stTransaction1);
//
//        // add two versions for this transaction with no issues
//        for (StagingTransactionRecipient stagingTransactionRecipient : stTransaction1.getRecipients().values()) {
//            StagingTransactionVersion stagingTransactionVersion = new StagingTransactionVersion();
//            stagingTransactionVersion.setId(stagingTransactionRecipient.getId());
//            stagingTransactionVersion.setPayload("PAYLOAD".getBytes());
//            stagingTransactionVersion.setTransaction(stTransaction1);
//            stTransaction1
//                    .getVersions()
//                    .put(stagingTransactionVersion.getId().getRecipient(), stagingTransactionVersion);
//        }
//
//        stEntityDAO.save(stTransaction1);
//        entityManager.flush();
//
//        final MessageHashStr txnHash2 = new MessageHashStr("TXN2".getBytes());
//
//        final StagingTransaction stTransaction2 = new StagingTransaction();
//        stTransaction2.setHash(txnHash2);
//        stTransaction2.setCipherText("CIPHERTEXT".getBytes());
//        stTransaction2.setCipherTextNonce("ONE".getBytes());
//        stTransaction2.setRecipientNonce("ONE".getBytes());
//        stTransaction2.setSenderKey("SENDER".getBytes());
//
//        addTransactionRecipients(stTransaction2);
//
//        StagingAffectedContractTransactionId stAffectedContractTransactionId21 =
//                new StagingAffectedContractTransactionId(txnHash2, txnHash1);
//        StagingAffectedContractTransaction stAffectedContractTransaction21 = new StagingAffectedContractTransaction();
//        stAffectedContractTransaction21.setId(stAffectedContractTransactionId21);
//        stAffectedContractTransaction21.setSecurityHash("SecureHash".getBytes());
//        stAffectedContractTransaction21.setSourceTransaction(stTransaction2);
//
//        stTransaction2.getAffectedContractTransactions().put(txnHash1, stAffectedContractTransaction21);
//
//        stEntityDAO.save(stTransaction2);
//        entityManager.flush();
//
//        final MessageHashStr txnHash4 = new MessageHashStr("TXN4".getBytes());
//        // we are storing a transaction TXN4 which depends on another transaction TXN3 (which has not been received yet)
//        final MessageHashStr txnHash3 = new MessageHashStr("TXN3".getBytes());
//
//        final StagingTransaction stTransaction4 = new StagingTransaction();
//        stTransaction4.setHash(txnHash4);
//        stTransaction4.setCipherText("CIPHERTEXT".getBytes());
//        stTransaction4.setCipherTextNonce("ONE".getBytes());
//        stTransaction4.setRecipientNonce("ONE".getBytes());
//        stTransaction4.setSenderKey("SENDER".getBytes());
//
//        addTransactionRecipients(stTransaction4);
//
//        StagingAffectedContractTransactionId stAffectedContractTransactionId43 =
//                new StagingAffectedContractTransactionId(txnHash4, txnHash3);
//        StagingAffectedContractTransaction stAffectedContractTransaction43 = new StagingAffectedContractTransaction();
//        stAffectedContractTransaction43.setId(stAffectedContractTransactionId43);
//        stAffectedContractTransaction43.setSecurityHash("SecureHash".getBytes());
//        stAffectedContractTransaction43.setSourceTransaction(stTransaction4);
//
//        stTransaction4.getAffectedContractTransactions().put(txnHash3, stAffectedContractTransaction43);
//
//        stEntityDAO.save(stTransaction4);
//        entityManager.flush();
//
//        final StagingTransaction stTransaction3 = new StagingTransaction();
//        stTransaction3.setHash(txnHash3);
//        stTransaction3.setCipherText("CIPHERTEXT".getBytes());
//        stTransaction3.setCipherTextNonce("ONE".getBytes());
//        stTransaction3.setRecipientNonce("ONE".getBytes());
//        stTransaction3.setSenderKey("SENDER".getBytes());
//
//        addTransactionRecipients(stTransaction3);
//
//        StagingAffectedContractTransactionId stAffectedContractTransactionId31 =
//                new StagingAffectedContractTransactionId(txnHash3, txnHash1);
//        StagingAffectedContractTransaction stAffectedContractTransaction31 = new StagingAffectedContractTransaction();
//        stAffectedContractTransaction31.setId(stAffectedContractTransactionId31);
//        stAffectedContractTransaction31.setSecurityHash("SecureHash".getBytes());
//        stAffectedContractTransaction31.setSourceTransaction(stTransaction3);
//
//        stTransaction3.getAffectedContractTransactions().put(txnHash1, stAffectedContractTransaction31);
//
//        stEntityDAO.save(stTransaction3);
//        entityManager.flush();
//
//        final MessageHashStr txnHash5 = new MessageHashStr("TXN5".getBytes());
//        // TXN5 is a unresolvable transaction as it depends on TXN6 which is never received
//        final MessageHashStr txnHash6 = new MessageHashStr("TXN6".getBytes());
//
//        final StagingTransaction stTransaction5 = new StagingTransaction();
//        stTransaction5.setHash(txnHash5);
//        stTransaction5.setCipherText("CIPHERTEXT".getBytes());
//        stTransaction5.setCipherTextNonce("ONE".getBytes());
//        stTransaction5.setRecipientNonce("ONE".getBytes());
//        stTransaction5.setSenderKey("SENDER".getBytes());
//
//        addTransactionRecipients(stTransaction5);
//
//        StagingAffectedContractTransactionId stAffectedContractTransactionId56 =
//                new StagingAffectedContractTransactionId(txnHash5, txnHash6);
//        StagingAffectedContractTransaction stAffectedContractTransaction56 = new StagingAffectedContractTransaction();
//        stAffectedContractTransaction56.setId(stAffectedContractTransactionId56);
//        stAffectedContractTransaction56.setSecurityHash("SecureHash".getBytes());
//        stAffectedContractTransaction56.setSourceTransaction(stTransaction5);
//
//        stTransaction5.getAffectedContractTransactions().put(txnHash6, stAffectedContractTransaction56);
//
//        stEntityDAO.save(stTransaction5);
//        entityManager.flush();
//
//        final MessageHashStr txnHash7 = new MessageHashStr("TXN7".getBytes());
//        // TXN7 depends on TXN1 and TXN3
//        final StagingTransaction stTransaction7 = new StagingTransaction();
//        stTransaction7.setHash(txnHash7);
//        stTransaction7.setCipherText("CIPHERTEXT".getBytes());
//        stTransaction7.setCipherTextNonce("ONE".getBytes());
//        stTransaction7.setRecipientNonce("ONE".getBytes());
//        stTransaction7.setSenderKey("SENDER".getBytes());
//
//        addTransactionRecipients(stTransaction7);
//
//        StagingAffectedContractTransactionId stAffectedContractTransactionId71 =
//                new StagingAffectedContractTransactionId(txnHash7, txnHash1);
//        StagingAffectedContractTransaction stAffectedContractTransaction71 = new StagingAffectedContractTransaction();
//        stAffectedContractTransaction71.setId(stAffectedContractTransactionId71);
//        stAffectedContractTransaction71.setSecurityHash("SecureHash".getBytes());
//        stAffectedContractTransaction71.setSourceTransaction(stTransaction7);
//
//        stTransaction7.getAffectedContractTransactions().put(txnHash1, stAffectedContractTransaction71);
//
//        StagingAffectedContractTransactionId stAffectedContractTransactionId74 =
//                new StagingAffectedContractTransactionId(txnHash7, txnHash4);
//        StagingAffectedContractTransaction stAffectedContractTransaction74 = new StagingAffectedContractTransaction();
//        stAffectedContractTransaction74.setId(stAffectedContractTransactionId74);
//        stAffectedContractTransaction74.setSecurityHash("SecureHash".getBytes());
//        stAffectedContractTransaction74.setSourceTransaction(stTransaction7);
//
//        stTransaction7.getAffectedContractTransactions().put(txnHash4, stAffectedContractTransaction74);
//
//        stEntityDAO.save(stTransaction7);
//        entityManager.flush();
//        entityManager.clear();
//    }
//
//    public static void testStagingTransactions(StagingEntityDAO stagingEntityDAO, EntityManager entityManager) {
//
//        final List<StagingTransaction> transactionsBeforeStaging =
//                stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);
//
//        transactionsBeforeStaging.forEach(
//                stagingTransaction -> {
//                    assertThat(stagingTransaction.getValidationStage()).isNull();
//                });
//
//        stagingEntityDAO.performStaging(Integer.MAX_VALUE);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        final List<StagingTransaction> verifiedTransactions =
//                stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);
//        assertThat(verifiedTransactions.get(0).getValidationStage()).isEqualTo(1L);
//        assertThat(verifiedTransactions.get(1).getValidationStage()).isEqualTo(2L);
//        assertThat(verifiedTransactions.get(2).getValidationStage()).isEqualTo(2L);
//        assertThat(verifiedTransactions.get(3).getValidationStage()).isEqualTo(3L);
//        assertThat(verifiedTransactions.get(4).getValidationStage()).isEqualTo(4L);
//        assertThat(verifiedTransactions.get(5).getValidationStage()).isNull();
//
//        final List<StagingTransaction> allTransactions =
//                stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);
//        // Transactions 1-4 and 7 are successfully resolved/staged. TXN5 is not - as it depends on an unknown
//        // transaction - TXN6.
//        assertThat(allTransactions.stream().filter(et -> et.getValidationStage() == null).count()).isEqualTo(1);
//        assertThat(stagingEntityDAO.countAll()).isEqualTo(6);
//        assertThat(stagingEntityDAO.countStaged()).isEqualTo(5);
//    }
//
//    public static void testRetrieveTransactionByHash(StagingEntityDAO stagingEntityDAO) {
//        final MessageHashStr txnHash7 = new MessageHashStr("TXN7".getBytes());
//        final Optional<StagingTransaction> stagingTransaction = stagingEntityDAO.retrieveByHash(txnHash7);
//
//        assertThat(stagingTransaction).isPresent();
//        assertThat(stagingTransaction.get().getAffectedContractTransactions()).hasSize(2);
//    }
//
//    public static void testUpdate(StagingEntityDAO stagingEntityDAO, EntityManager entityManager) {
//        final MessageHashStr txnHash7 = new MessageHashStr("TXN7".getBytes());
//        final Optional<StagingTransaction> stagingTransaction = stagingEntityDAO.retrieveByHash(txnHash7);
//
//        assertThat(stagingTransaction).isPresent();
//        StagingTransaction st = stagingTransaction.get();
//        st.setValidationStage(123L);
//        stagingEntityDAO.update(st);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        final Optional<StagingTransaction> stagingTransactionAfterUpdate = stagingEntityDAO.retrieveByHash(txnHash7);
//        assertThat(stagingTransactionAfterUpdate).isPresent();
//
//        assertThat(stagingTransactionAfterUpdate.get().getValidationStage()).isEqualTo(123L);
//    }
//
//    public static void testDeleteTransactionByHash(StagingEntityDAO stagingEntityDAO) {
//        final MessageHashStr txnHash1 = new MessageHashStr("TXN1".getBytes());
//
//        final Optional<StagingTransaction> before = stagingEntityDAO.retrieveByHash(txnHash1);
//
//        assertThat(before).isPresent();
//
//        stagingEntityDAO.delete(txnHash1);
//
//        final Optional<StagingTransaction> stagingTransaction = stagingEntityDAO.retrieveByHash(txnHash1);
//
//        assertThat(stagingTransaction).isNotPresent();
//    }
//
//    public static void testCleanStagingData(StagingEntityDAO stagingEntityDAO) {
//        stagingEntityDAO.cleanStagingArea(Integer.MAX_VALUE);
//
//        final List<StagingTransaction> transactions =
//                stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(0, Integer.MAX_VALUE);
//
//        assertThat(transactions).isEmpty();
//    }
//
//    @RunWith(SpringRunner.class)
//    @ContextConfiguration(classes = JpaH2Config.class)
//    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//    public static class H2Test {
//
//        @PersistenceContext private EntityManager entityManager;
//
//        @Inject private StagingEntityDAO stEntityDAO;
//
//        @Inject private PlatformTransactionManager txManager;
//
//        private TransactionStatus txStatus;
//
//        @Before
//        public void init() throws Exception {
//            TransactionStatus transaction =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//            StagingEntityDAOTest.initialiseTransactions(entityManager, stEntityDAO);
//            txManager.commit(transaction);
//            this.txStatus =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//        }
//
//        @After
//        public void clear() throws Exception {
//            if (txStatus.isRollbackOnly()) {
//                txManager.rollback(txStatus);
//            } else {
//                txManager.commit(txStatus);
//            }
//            TransactionStatus transaction =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//            stEntityDAO.cleanStagingArea(Integer.MAX_VALUE);
//            txManager.commit(transaction);
//        }
//
//        @Test
//        public void saveDoesntAllowNullCipherText() {
//
//            StagingTransaction stTransaction = new StagingTransaction();
//            stTransaction.setHash(new MessageHashStr(new byte[] {5}));
//
//            final Throwable throwable =
//                    catchThrowable(
//                            () -> {
//                                stEntityDAO.save(stTransaction);
//                                entityManager.flush();
//                            });
//
//            assertThat(throwable)
//                    .isInstanceOf(PersistenceException.class)
//                    .hasMessageContaining("NULL not allowed for column \"CIPHER_TEXT\"");
//        }
//
//        @Test
//        public void testStagingQuery() {
//            StagingEntityDAOTest.testStagingTransactions(stEntityDAO, entityManager);
//        }
//
//        @Test
//        public void testRetrieveTransactionByHash() {
//            StagingEntityDAOTest.testRetrieveTransactionByHash(stEntityDAO);
//        }
//
//        @Test
//        public void testDeleteTransactionByHash() {
//            StagingEntityDAOTest.testDeleteTransactionByHash(stEntityDAO);
//        }
//
//        @Test
//        public void testUpdate() {
//            StagingEntityDAOTest.testUpdate(stEntityDAO, entityManager);
//        }
//
//        @Test
//        public void testCleanupStagingData() {
//            testCleanStagingData(stEntityDAO);
//        }
//    }
//
//    @RunWith(SpringRunner.class)
//    @ContextConfiguration(classes = JpaHsqlConfig.class)
//    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//    public static class HsqlTest {
//
//        @PersistenceContext private EntityManager entityManager;
//
//        @Inject private StagingEntityDAO stEntityDAO;
//
//        @Inject private PlatformTransactionManager txManager;
//
//        private TransactionStatus txStatus;
//
//        @Before
//        public void init() throws Exception {
//            TransactionStatus transaction =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//            StagingEntityDAOTest.initialiseTransactions(entityManager, stEntityDAO);
//            txManager.commit(transaction);
//            this.txStatus =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//        }
//
//        @After
//        public void clear() throws Exception {
//            if (txStatus.isRollbackOnly()) {
//                txManager.rollback(txStatus);
//            } else {
//                txManager.commit(txStatus);
//            }
//            TransactionStatus transaction =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//            stEntityDAO.cleanStagingArea(Integer.MAX_VALUE);
//            txManager.commit(transaction);
//        }
//
//        @Test
//        public void saveDoesntAllowNullCipherText() {
//
//            StagingTransaction stTransaction = new StagingTransaction();
//            stTransaction.setHash(new MessageHashStr(new byte[] {5}));
//
//            final Throwable throwable =
//                    catchThrowable(
//                            () -> {
//                                stEntityDAO.save(stTransaction);
//                                entityManager.flush();
//                            });
//
//            assertThat(throwable)
//                    .isInstanceOf(PersistenceException.class)
//                    .hasMessageContaining(
//                            "NOT NULL check constraint; SYS_CT_10110 table: ST_TRANSACTION column: CIPHER_TEXT");
//        }
//
//        @Test
//        public void testStagingQuery() {
//            StagingEntityDAOTest.testStagingTransactions(stEntityDAO, entityManager);
//        }
//
//        @Test
//        public void testRetrieveTransactionByHash() {
//            StagingEntityDAOTest.testRetrieveTransactionByHash(stEntityDAO);
//        }
//
//        @Test
//        public void testDeleteTransactionByHash() {
//            StagingEntityDAOTest.testDeleteTransactionByHash(stEntityDAO);
//        }
//
//        @Test
//        public void testUpdate() {
//            StagingEntityDAOTest.testUpdate(stEntityDAO, entityManager);
//        }
//
//        @Test
//        public void testCleanupStagingData() {
//            testCleanStagingData(stEntityDAO);
//        }
//    }
//
//    @RunWith(SpringRunner.class)
//    @ContextConfiguration(classes = JpaSqliteConfig.class)
//    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//    public static class SqliteTest {
//
//        @PersistenceContext private EntityManager entityManager;
//
//        @Inject private StagingEntityDAO stEntityDAO;
//
//        @Inject private PlatformTransactionManager txManager;
//
//        private TransactionStatus txStatus;
//
//        @Before
//        public void init() throws Exception {
//            TransactionStatus transaction =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//            StagingEntityDAOTest.initialiseTransactions(entityManager, stEntityDAO);
//            txManager.commit(transaction);
//            this.txStatus =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//        }
//
//        @After
//        public void clear() throws Exception {
//            if (txStatus.isRollbackOnly()) {
//                txManager.rollback(txStatus);
//            } else {
//                txManager.commit(txStatus);
//            }
//            TransactionStatus transaction =
//                    txManager.getTransaction(
//                            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//            stEntityDAO.cleanStagingArea(Integer.MAX_VALUE);
//            txManager.commit(transaction);
//        }
//
//        @Test
//        public void saveDoesntAllowNullCipherText() {
//
//            StagingTransaction stTransaction = new StagingTransaction();
//            stTransaction.setHash(new MessageHashStr(new byte[] {5}));
//
//            final Throwable throwable =
//                    catchThrowable(
//                            () -> {
//                                stEntityDAO.save(stTransaction);
//                                entityManager.flush();
//                            });
//
//            assertThat(throwable)
//                    .isInstanceOf(PersistenceException.class)
//                    .hasMessageContaining(
//                            "A NOT NULL constraint failed (NOT NULL constraint failed: ST_TRANSACTION.CIPHER_TEXT)");
//        }
//
//        @Test
//        public void testStagingQuery() {
//            StagingEntityDAOTest.testStagingTransactions(stEntityDAO, entityManager);
//        }
//
//        @Test
//        public void testRetrieveTransactionByHash() {
//            StagingEntityDAOTest.testRetrieveTransactionByHash(stEntityDAO);
//        }
//
//        @Test
//        public void testDeleteTransactionByHash() {
//            StagingEntityDAOTest.testDeleteTransactionByHash(stEntityDAO);
//        }
//
//        @Test
//        public void testUpdate() {
//            StagingEntityDAOTest.testUpdate(stEntityDAO, entityManager);
//        }
//
//        @Test
//        public void testCleanupStagingData() {
//            testCleanStagingData(stEntityDAO);
//        }
//    }
//}
