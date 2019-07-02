package com.quorum.tessera.transaction;

import com.quorum.tessera.dao.JpaH2Config;
import com.quorum.tessera.dao.JpaHsqlConfig;
import com.quorum.tessera.dao.JpaSqliteConfig;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.transaction.model.EncryptedRawTransaction;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    EncryptedRawTransactionDAOTest.H2Test.class,
    EncryptedRawTransactionDAOTest.HsqlTest.class,
    EncryptedRawTransactionDAOTest.SqliteTest.class
})
public class EncryptedRawTransactionDAOTest {

    @Transactional
    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = JpaH2Config.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    public static class H2Test {

        @PersistenceContext private EntityManager entityManager;

        @Inject private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

        @Test
        public void saveDoesntAllowNullEncyptedPayload() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NULL not allowed for column \"ENCRYPTED_PAYLOAD\"");
        }

        @Test
        public void saveDoesntAllowNullHash() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NULL not allowed for column \"HASH\"");
        }

        @Test
        public void saveDoesntAllowNullNonce() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NULL not allowed for column \"NONCE\"");
        }

        @Test
        public void saveDoesntAllowNullEncryptedKey() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NULL not allowed for column \"ENCRYPTED_KEY\"");
        }

        @Test
        public void saveDoesntAllowNullSender() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setEncryptedKey("key".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NULL not allowed for column \"SENDER\"");
        }

        @Test
        public void cannotPersistMultipleOfSameHash() {

            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);

            final EncryptedRawTransaction duplicateTransaction = new EncryptedRawTransaction();
            duplicateTransaction.setEncryptedPayload(new byte[] {6});
            duplicateTransaction.setHash(new MessageHash(new byte[] {1}));
            duplicateTransaction.setEncryptedKey("key".getBytes());
            duplicateTransaction.setNonce("nonce".getBytes());
            duplicateTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(duplicateTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("Unique index or primary key violation")
                    .hasMessageContaining("ENCRYPTED_RAW_TRANSACTION(HASH)");
        }

        @Test
        public void validEncryptedRawTransactionCanBePersisted() {

            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());

            assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedRawTransaction);
        }

        @Test
        public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

            // put a transaction in the database
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);

            // check that it is actually in the database
            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());
            assertThat(retrieved).isNotNull();

            // delete the transaction
            encryptedRawTransactionDAO.delete(new MessageHash(new byte[] {1}));

            // check it is not longer in the database
            final EncryptedRawTransaction deleted =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());
            assertThat(deleted).isNull();
        }

        @Test(expected = EntityNotFoundException.class)
        public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
            // delete the transaction
            encryptedRawTransactionDAO.delete(new MessageHash(new byte[] {1}));
        }

        @Test
        public void retrieveByHashFindsTransactionThatIsPresent() {
            // put a transaction in the database
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            final MessageHash searchHash = new MessageHash(new byte[] {1});

            final Optional<EncryptedRawTransaction> retrieved = encryptedRawTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isTrue();
            assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedRawTransaction);
        }

        @Test
        public void retrieveByHashReturnsEmptyOptionalWhenNotPresent() {
            final MessageHash searchHash = new MessageHash(new byte[] {1});

            final Optional<EncryptedRawTransaction> retrieved = encryptedRawTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isFalse();
        }

        @Test
        public void persistAddsTimestampToEntity() {
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final long expected = System.currentTimeMillis();
            encryptedRawTransactionDAO.save(encryptedRawTransaction);

            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTimestamp()).isNotZero();
        }
    }

    @Transactional
    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = JpaHsqlConfig.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    public static class HsqlTest {

        @PersistenceContext private EntityManager entityManager;

        @Inject private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

        @Test
        public void saveDoesntAllowNullEncodedPayload() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("integrity constraint violation: NOT NULL check constraint");
        }

        @Test
        public void saveDoesntAllowNullHash() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("integrity constraint violation: NOT NULL check constraint");
        }

        public void saveDoesntAllowNullNonce() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setEncryptedKey("key".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("integrity constraint violation: NOT NULL check constraint");
        }

        @Test
        public void saveDoesntAllowNullEncryptedKey() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("integrity constraint violation: NOT NULL check constraint");
        }

        @Test
        public void saveDoesntAllowNullSender() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setEncryptedKey("key".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("integrity constraint violation: NOT NULL check constraint");
        }

        @Test
        public void cannotPersistMultipleOfSameHash() {

            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            final EncryptedRawTransaction duplicateTransaction = new EncryptedRawTransaction();
            duplicateTransaction.setEncryptedPayload(new byte[] {6});
            duplicateTransaction.setHash(new MessageHash(new byte[] {1}));
            duplicateTransaction.setEncryptedKey("key".getBytes());
            duplicateTransaction.setNonce("nonce".getBytes());
            duplicateTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(duplicateTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("unique constraint or index violation")
                    .hasMessageContaining("ENCRYPTED_RAW_TRANSACTION");
        }

        @Test
        public void validEncryptedRawTransactionCanBePersisted() {

            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());

            assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedRawTransaction);
        }

        @Test
        public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

            // put a transaction in the database
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            // check that it is actually in the database
            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());
            assertThat(retrieved).isNotNull();

            // delete the transaction
            encryptedRawTransactionDAO.delete(new MessageHash(new byte[] {1}));
            entityManager.flush();

            // check it is not longer in the database
            final EncryptedRawTransaction deleted =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());
            assertThat(deleted).isNull();
        }

        @Test(expected = EntityNotFoundException.class)
        public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
            // delete the transaction
            encryptedRawTransactionDAO.delete(new MessageHash(new byte[] {1}));
        }

        @Test
        public void retrieveByHashFindsTransactionThatIsPresent() {
            // put a transaction in the database
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            final MessageHash searchHash = new MessageHash(new byte[] {1});

            final Optional<EncryptedRawTransaction> retrieved = encryptedRawTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isTrue();
            assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedRawTransaction);
        }

        @Test
        public void retrieveByHashReturnsEmptyOptionalWhenNotPresent() {
            final MessageHash searchHash = new MessageHash(new byte[] {1});

            final Optional<EncryptedRawTransaction> retrieved = encryptedRawTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isFalse();
        }

        @Test
        public void persistAddsTimestampToEntity() {
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final long expected = System.currentTimeMillis();
            encryptedRawTransactionDAO.save(encryptedRawTransaction);

            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTimestamp()).isNotZero();
        }
    }

    @Transactional
    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = JpaSqliteConfig.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    public static class SqliteTest {

        @PersistenceContext private EntityManager entityManager;

        @Inject private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

        @Test
        public void saveDoesntAllowNullEncodedPayload() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NOT NULL constraint failed");
        }

        @Test
        public void saveDoesntAllowNullHash() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NOT NULL constraint failed");
        }

        public void saveDoesntAllowNullNonce() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setEncryptedKey("key".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NOT NULL constraint failed");
        }

        @Test
        public void saveDoesntAllowNullEncryptedKey() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NOT NULL constraint failed");
        }

        @Test
        public void saveDoesntAllowNullSender() {

            EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {5}));
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setEncryptedKey("key".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(encryptedRawTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("NOT NULL constraint failed");
        }

        @Test
        public void cannotPersistMultipleOfSameHash() {

            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            final EncryptedRawTransaction duplicateTransaction = new EncryptedRawTransaction();
            duplicateTransaction.setEncryptedPayload(new byte[] {6});
            duplicateTransaction.setHash(new MessageHash(new byte[] {1}));
            duplicateTransaction.setEncryptedKey("key".getBytes());
            duplicateTransaction.setNonce("nonce".getBytes());
            duplicateTransaction.setSender("from".getBytes());

            final Throwable throwable =
                    catchThrowable(
                            () -> {
                                encryptedRawTransactionDAO.save(duplicateTransaction);
                                entityManager.flush();
                            });

            assertThat(throwable)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining("UNIQUE constraint failed")
                    .hasMessageContaining("ENCRYPTED_RAW_TRANSACTION.HASH");
        }

        @Test
        public void validEncryptedRawTransactionCanBePersisted() {

            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);
            entityManager.flush();

            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());

            assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedRawTransaction);
        }

        @Test
        public void deleteTransactionRemovesFromDatabaseAndReturnsTrue() {

            // put a transaction in the database
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);

            // check that it is actually in the database
            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());
            assertThat(retrieved).isNotNull();

            // delete the transaction
            encryptedRawTransactionDAO.delete(new MessageHash(new byte[] {1}));

            // check it is not longer in the database
            final EncryptedRawTransaction deleted =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());
            assertThat(deleted).isNull();
        }

        @Test(expected = EntityNotFoundException.class)
        public void deleteThrowsEntityNotFoundExceptionForNonExistentHash() {
            // delete the transaction
            encryptedRawTransactionDAO.delete(new MessageHash(new byte[] {1}));
        }

        @Test
        public void retrieveByHashFindsTransactionThatIsPresent() {
            // put a transaction in the database
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());
            encryptedRawTransactionDAO.save(encryptedRawTransaction);

            final MessageHash searchHash = new MessageHash(new byte[] {1});

            final Optional<EncryptedRawTransaction> retrieved = encryptedRawTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isTrue();
            assertThat(retrieved.get()).isEqualToComparingFieldByField(encryptedRawTransaction);
        }

        @Test
        public void retrieveByHashThrowsExceptionWhenNotPresent() {
            final MessageHash searchHash = new MessageHash(new byte[] {1});

            final Optional<EncryptedRawTransaction> retrieved = encryptedRawTransactionDAO.retrieveByHash(searchHash);

            assertThat(retrieved.isPresent()).isFalse();
        }

        @Test
        public void persistAddsTimestampToEntity() {
            final EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction();
            encryptedRawTransaction.setEncryptedPayload(new byte[] {5});
            encryptedRawTransaction.setHash(new MessageHash(new byte[] {1}));
            encryptedRawTransaction.setEncryptedKey("key".getBytes());
            encryptedRawTransaction.setNonce("nonce".getBytes());
            encryptedRawTransaction.setSender("from".getBytes());

            final long expected = System.currentTimeMillis();
            encryptedRawTransactionDAO.save(encryptedRawTransaction);

            final EncryptedRawTransaction retrieved =
                    entityManager.find(EncryptedRawTransaction.class, encryptedRawTransaction.getHash());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTimestamp()).isNotZero();
        }
    }
}
