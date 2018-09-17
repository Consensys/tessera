package com.quorum.tessera.transaction;

import com.quorum.tessera.dao.JpaH2Config;
import com.quorum.tessera.dao.JpaHsqlConfig;
import com.quorum.tessera.dao.JpaSqliteConfig;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        EncryptedTransactionDAOTest.H2Test.class,
        EncryptedTransactionDAOTest.HsqlTest.class,
        EncryptedTransactionDAOTest.SqliteTest.class})
public class EncryptedTransactionDAOTest {

    @Transactional
    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = JpaH2Config.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    public static class H2Test {

        @PersistenceContext
        private EntityManager entityManager;

        @Inject
        private EncryptedTransactionDAO encryptedTransactionDAO;

        @Test
        public void saveDoesntAllowNullEncodedPayload() {

            EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setHash(new MessageHash(new byte[]{5}));

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(encryptedTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("NULL not allowed for column \"ENCODED_PAYLOAD\"");

        }

        @Test
        public void saveDoesntAllowNullHash() {

            EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(encryptedTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("NULL not allowed for column \"HASH\"");

        }

        @Test
        public void cannotPersistMultipleOfSameHash() {

            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
            duplicateTransaction.setEncodedPayload(new byte[]{6});
            duplicateTransaction.setHash(new MessageHash(new byte[]{1}));

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(duplicateTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("Unique index or primary key violation")
                .hasMessageContaining("ENCRYPTED_TRANSACTION(HASH)");

        }

        @Test
        public void validEncryptedTransactionCanBePersisted() {

            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

            assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);

        }

        @Test
        public void fetchingAllTransactionsReturnsAll() {

            final List<EncryptedTransaction> payloads = IntStream.range(0, 50)
                .mapToObj(i -> new EncryptedTransaction(
                        new MessageHash(new byte[]{(byte) i}),
                        new byte[]{(byte) i}
                    )
                ).peek(entityManager::persist)
                .collect(Collectors.toList());

            final List<EncryptedTransaction> retrievedList = encryptedTransactionDAO.retrieveAllTransactions();

            assertThat(retrievedList).hasSameSizeAs(payloads);
            assertThat(retrievedList).hasSameElementsAs(payloads);

        }

        @Test
        public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

            //put a transaction in the database
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            //check that it is actually in the database
            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());
            assertThat(retrieved).isNotNull();

            //delete the transaction
            encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));

            //check it is not longer in the database
            final EncryptedTransaction deleted
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());
            assertThat(deleted).isNull();
        }

        @Test(expected = EntityNotFoundException.class)
        public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
            //delete the transaction
            encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));
        }

        @Test
        public void retrieveByHashFindsTransactionThatIsPresent() {
            //put a transaction in the database
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final MessageHash searchHash = new MessageHash(new byte[]{1});

            final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isTrue();
            assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedTransaction);
        }

        @Test
        public void retrieveByHashThrowsExceptionWhenNotPresent() {
            final MessageHash searchHash = new MessageHash(new byte[]{1});

            final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isFalse();
        }

        @Test
        public void persistAddsTimestampToEntity() {
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));

            final long expected = System.currentTimeMillis();
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTimestamp()).isNotZero();
        }
    }

    @Transactional
    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = JpaHsqlConfig.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    public static class HsqlTest {

        @PersistenceContext
        private EntityManager entityManager;

        @Inject
        private EncryptedTransactionDAO encryptedTransactionDAO;

        @Test
        public void saveDoesntAllowNullEncodedPayload() {

            EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setHash(new MessageHash(new byte[]{5}));

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(encryptedTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("integrity constraint violation: NOT NULL check constraint");

        }

        @Test
        public void saveDoesntAllowNullHash() {

            EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(encryptedTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("integrity constraint violation: NOT NULL check constraint");

        }

        @Test
        public void cannotPersistMultipleOfSameHash() {

            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
            duplicateTransaction.setEncodedPayload(new byte[]{6});
            duplicateTransaction.setHash(new MessageHash(new byte[]{1}));

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(duplicateTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("unique constraint or index violation")
                .hasMessageContaining("ENCRYPTED_TRANSACTION");

        }

        @Test
        public void validEncryptedTransactionCanBePersisted() {

            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

            assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);

        }

        @Test
        public void fetchingAllTransactionsReturnsAll() {

            final List<EncryptedTransaction> payloads = IntStream.range(0, 50)
                .mapToObj(i -> new EncryptedTransaction(
                        new MessageHash(new byte[]{(byte) i}),
                        new byte[]{(byte) i}
                    )
                ).peek(entityManager::persist)
                .collect(Collectors.toList());

            final List<EncryptedTransaction> retrievedList = encryptedTransactionDAO.retrieveAllTransactions();

            assertThat(retrievedList).hasSameSizeAs(payloads);
            assertThat(retrievedList).hasSameElementsAs(payloads);

        }

        @Test
        public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

            //put a transaction in the database
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            //check that it is actually in the database
            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());
            assertThat(retrieved).isNotNull();

            //delete the transaction
            encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));

            //check it is not longer in the database
            final EncryptedTransaction deleted
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());
            assertThat(deleted).isNull();
        }

        @Test(expected = EntityNotFoundException.class)
        public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
            //delete the transaction
            encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));
        }

        @Test
        public void retrieveByHashFindsTransactionThatIsPresent() {
            //put a transaction in the database
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final MessageHash searchHash = new MessageHash(new byte[]{1});

            final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isTrue();
            assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedTransaction);
        }

        @Test
        public void retrieveByHashThrowsExceptionWhenNotPresent() {
            final MessageHash searchHash = new MessageHash(new byte[]{1});

            final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isFalse();
        }

        @Test
        public void persistAddsTimestampToEntity() {
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));

            final long expected = System.currentTimeMillis();
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTimestamp()).isNotZero();
        }
    }


    @Transactional
    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = JpaSqliteConfig.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    public static class SqliteTest {

        @PersistenceContext
        private EntityManager entityManager;

        @Inject
        private EncryptedTransactionDAO encryptedTransactionDAO;

        @Test
        public void saveDoesntAllowNullEncodedPayload() {

            EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setHash(new MessageHash(new byte[]{5}));

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(encryptedTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("NOT NULL constraint failed");

        }

        @Test
        public void saveDoesntAllowNullHash() {

            EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(encryptedTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("NOT NULL constraint failed");

        }

        @Test
        public void cannotPersistMultipleOfSameHash() {

            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
            duplicateTransaction.setEncodedPayload(new byte[]{6});
            duplicateTransaction.setHash(new MessageHash(new byte[]{1}));

            final Throwable throwable = catchThrowable(() -> {
                encryptedTransactionDAO.save(duplicateTransaction);
                entityManager.flush();
            });

            assertThat(throwable)
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("UNIQUE constraint failed")
                .hasMessageContaining("ENCRYPTED_TRANSACTION.HASH");

        }

        @Test
        public void validEncryptedTransactionCanBePersisted() {

            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

            assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);

        }

        @Test
        public void fetchingAllTransactionsReturnsAll() {

            final List<EncryptedTransaction> payloads = IntStream.range(0, 50)
                .mapToObj(i -> new EncryptedTransaction(
                        new MessageHash(new byte[]{(byte) i}),
                        new byte[]{(byte) i}
                    )
                ).peek(entityManager::persist)
                .collect(Collectors.toList());

            final List<EncryptedTransaction> retrievedList = encryptedTransactionDAO.retrieveAllTransactions();

            assertThat(retrievedList).hasSameSizeAs(payloads);
            assertThat(retrievedList).hasSameElementsAs(payloads);

        }

        @Test
        public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

            //put a transaction in the database
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            //check that it is actually in the database
            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());
            assertThat(retrieved).isNotNull();

            //delete the transaction
            encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));

            //check it is not longer in the database
            final EncryptedTransaction deleted
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());
            assertThat(deleted).isNull();
        }

        @Test(expected = EntityNotFoundException.class)
        public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
            //delete the transaction
            encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));
        }

        @Test
        public void retrieveByHashFindsTransactionThatIsPresent() {
            //put a transaction in the database
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
            encryptedTransactionDAO.save(encryptedTransaction);

            final MessageHash searchHash = new MessageHash(new byte[]{1});

            final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isTrue();
            assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedTransaction);
        }

        @Test
        public void retrieveByHashThrowsExceptionWhenNotPresent() {
            final MessageHash searchHash = new MessageHash(new byte[]{1});

            final Optional<EncryptedTransaction> retrieved = encryptedTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isFalse();
        }

        @Test
        public void persistAddsTimestampToEntity() {
            final EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
            encryptedTransaction.setEncodedPayload(new byte[]{5});
            encryptedTransaction.setHash(new MessageHash(new byte[]{1}));

            final long expected = System.currentTimeMillis();
            encryptedTransactionDAO.save(encryptedTransaction);

            final EncryptedTransaction retrieved
                = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getHash());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTimestamp()).isNotZero();
        }
    }
}


