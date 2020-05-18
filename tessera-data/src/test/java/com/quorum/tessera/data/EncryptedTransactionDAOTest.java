package com.quorum.tessera.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@RunWith(Parameterized.class)
public class EncryptedTransactionDAOTest {

    private EntityManagerFactory entityManagerFactory;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private TestConfig testConfig;

    public EncryptedTransactionDAOTest(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Before
    public void onSetUp() {

        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url", testConfig.getUrl());
        properties.put("javax.persistence.jdbc.user", "junit");
        properties.put("javax.persistence.jdbc.password", "");
        properties.put("eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        properties.put("eclipselink.logging.level", "FINE");
        properties.put("eclipselink.logging.parameters", "true");
        properties.put("eclipselink.logging.level.sql", "FINE");
        properties.put("eclipselink.cache.shared.default", "false");
        properties.put("javax.persistence.schema-generation.database.action", "drop-and-create");

        entityManagerFactory = Persistence.createEntityManagerFactory("tessera", properties);
        encryptedTransactionDAO = new EncryptedTransactionDAOImpl(entityManagerFactory);
    }

    @After
    public void onTearDown() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from EncryptedTransaction").executeUpdate();
        entityManager.getTransaction().commit();
    }

    @Test
    public void saveDoesntAllowNullEncodedPayload() {

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(new MessageHash(new byte[] {5}));

        try {
            encryptedTransactionDAO.save(encryptedTransaction);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        } catch (PersistenceException ex) {
            String expectedMessage = String.format(testConfig.getRequiredFieldColumTemplate(), "ENCODED_PAYLOAD");

            assertThat(ex)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining(expectedMessage)
                    .hasMessageContaining("ENCODED_PAYLOAD");
        }
    }

    @Test
    public void updateTransaction() {

        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(new byte[] {5});
        encryptedTransaction.setHash(new MessageHash(new byte[] {1}));
        encryptedTransactionDAO.save(encryptedTransaction);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        final EncryptedTransaction retrieved =
                entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

        assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);

        encryptedTransaction.setEncodedPayload(new byte[] {6});
        encryptedTransactionDAO.update(encryptedTransaction);

        entityManager.getTransaction().rollback();

        entityManager.getTransaction().begin();

        final EncryptedTransaction after =
                entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

        assertThat(after).isEqualToComparingFieldByField(encryptedTransaction);

        entityManager.getTransaction().rollback();
    }

    @Test
    public void saveDoesntAllowNullHash() {

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(new byte[] {5});

        try {
            encryptedTransactionDAO.save(encryptedTransaction);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        } catch (PersistenceException ex) {
            String expectedMessage = String.format(testConfig.getRequiredFieldColumTemplate(), "HASH");

            assertThat(ex)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining(expectedMessage)
                    .hasMessageContaining("HASH");
        }
    }

    @Test
    public void cannotPersistMultipleOfSameHash() {

        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(new byte[] {5});
        encryptedTransaction.setHash(new MessageHash(new byte[] {1}));

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(encryptedTransaction);
        entityManager.getTransaction().commit();

        final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
        duplicateTransaction.setEncodedPayload(new byte[] {6});
        duplicateTransaction.setHash(new MessageHash(new byte[] {1}));

        try {
            encryptedTransactionDAO.save(encryptedTransaction);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        } catch (PersistenceException ex) {
            assertThat(ex)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining(testConfig.getUniqueContraintViolationMessage());
        }
    }

    @Test
    public void validEncryptedTransactionCanBePersisted() {

        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(new byte[] {5});
        encryptedTransaction.setHash(new MessageHash(new byte[] {1}));
        encryptedTransactionDAO.save(encryptedTransaction);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        final EncryptedTransaction retrieved =
                entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

        assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);
        entityManager.getTransaction().rollback();
    }

    @Test
    public void fetchingAllTransactionsReturnsAll() {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        final List<EncryptedTransaction> payloads =
                IntStream.range(0, 50)
                        .mapToObj(i -> UUID.randomUUID().toString().getBytes())
                        .map(MessageHash::new)
                        .map(hash -> new EncryptedTransaction(hash, hash.getHashBytes()))
                        .peek(entityManager::persist)
                        .collect(Collectors.toList());

        entityManager.getTransaction().commit();

        final List<EncryptedTransaction> retrievedList =
                encryptedTransactionDAO.retrieveTransactions(0, Integer.MAX_VALUE);

        assertThat(encryptedTransactionDAO.transactionCount()).isEqualTo(payloads.size());
        assertThat(retrievedList).hasSameSizeAs(payloads);
        assertThat(retrievedList).hasSameElementsAs(payloads);
    }

    @Test
    public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

        final MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        // put a transaction in the database
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(new byte[] {5});
        encryptedTransaction.setHash(messageHash);

        entityManager.getTransaction().begin();
        entityManager.persist(encryptedTransaction);
        entityManager.getTransaction().commit();

        Query countQuery =
                entityManager.createQuery("select count(t) from EncryptedTransaction t where t.hash = :hash");
        Long result = (Long) countQuery.setParameter("hash", messageHash).getSingleResult();
        assertThat(result).isEqualTo(1L);

        encryptedTransactionDAO.delete(messageHash);

        // check it is not longer in the database
        Long result2 = (Long) countQuery.setParameter("hash", messageHash).getSingleResult();
        assertThat(result2).isZero();
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
        // delete the transaction
        final MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

        encryptedTransactionDAO.delete(messageHash);
    }

    @Test
    public void retrieveByHashFindsTransactionThatIsPresent() {
        // put a transaction in the database
        MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());

        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(new byte[] {5});
        encryptedTransaction.setHash(messageHash);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(encryptedTransaction);
        entityManager.getTransaction().commit();

        final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(messageHash);

        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedTransaction);
    }

    @Test
    public void retrieveByHashThrowsExceptionWhenNotPresent() {
        MessageHash searchHash = new MessageHash(UUID.randomUUID().toString().getBytes());

        final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(searchHash);

        assertThat(retrieved.isPresent()).isFalse();
    }

    @Test
    public void persistAddsTimestampToEntity() {
        MessageHash messageHash = new MessageHash(UUID.randomUUID().toString().getBytes());
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(new byte[] {5});
        encryptedTransaction.setHash(messageHash);

        final long expected = System.currentTimeMillis();
        encryptedTransactionDAO.save(encryptedTransaction);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        final EncryptedTransaction retrieved = entityManager.find(EncryptedTransaction.class, messageHash);
        entityManager.getTransaction().commit();

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTimestamp()).isNotZero();
    }

    @Test
    public void findByHashes() {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<EncryptedTransaction> transactions =
                IntStream.range(0, 100)
                        .mapToObj(i -> UUID.randomUUID().toString().getBytes())
                        .map(MessageHash::new)
                        .map(
                                h -> {
                                    EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
                                    encryptedTransaction.setHash(h);
                                    encryptedTransaction.setEncodedPayload(UUID.randomUUID().toString().getBytes());
                                    entityManager.persist(encryptedTransaction);
                                    return encryptedTransaction;
                                })
                        .collect(Collectors.toList());

        entityManager.getTransaction().commit();

        Collection<MessageHash> hashes =
                transactions.stream().map(EncryptedTransaction::getHash).collect(Collectors.toList());
        List<EncryptedTransaction> results = encryptedTransactionDAO.findByHashes(hashes);

        assertThat(results).isNotEmpty().containsExactlyInAnyOrderElementsOf(transactions);
    }

    @Test
    public void findByHashesEmpty() {

        List<EncryptedTransaction> results = encryptedTransactionDAO.findByHashes(Collections.EMPTY_LIST);

        assertThat(results).isEmpty();
    }

    @Parameterized.Parameters(name = "DB {0}")
    public static Collection<TestConfig> connectionDetails() {
        return List.of(TestConfig.values());
    }
}
