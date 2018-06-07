package com.github.nexus.transaction;

import com.github.nexus.dao.JpaConfig;
import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.transaction.model.EncryptedTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Transactional
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JpaConfig.class)
public class EncryptedTransactionDAOTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private EncryptedTransactionDAO encryptedTransactionDAO;

    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void saveDoesntAllowNullEncodedPayload() {

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(new byte[]{5});

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
        encryptedTransaction.setHash(new byte[]{1});
        encryptedTransactionDAO.save(encryptedTransaction);

        final EncryptedTransaction duplicateTransaction = new EncryptedTransaction();
        duplicateTransaction.setEncodedPayload(new byte[]{6});
        duplicateTransaction.setHash(new byte[]{1});

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
        encryptedTransaction.setHash(new byte[]{1});
        encryptedTransactionDAO.save(encryptedTransaction);

        final EncryptedTransaction retrieved
            = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getId());

        assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);

    }

    @Test
    public void fetchingAllTransactionsReturnsAll() {

        final List<EncryptedTransaction> payloads = IntStream.range(0, 50)
            .mapToObj(i -> new EncryptedTransaction(new byte[]{(byte) i}, new byte[]{(byte) i}))
            .peek(entityManager::persist)
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
        encryptedTransaction.setHash(new byte[]{1});
        encryptedTransactionDAO.save(encryptedTransaction);

        //check that it is actually in the database
        final EncryptedTransaction retrieved
            = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getId());
        assertThat(retrieved).isNotNull();

        //delete the transaction
        final boolean deletedFlag = encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));
        assertThat(deletedFlag).isTrue();

        //check it is not longer in the database
        final EncryptedTransaction deleted
            = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getId());
        assertThat(deleted).isNull();
    }

    @Test
    public void deleteReturnsFalseIfNotExist() {
        //delete the transaction
        final boolean deletedFlag = encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));
        assertThat(deletedFlag).isFalse();
    }

}
