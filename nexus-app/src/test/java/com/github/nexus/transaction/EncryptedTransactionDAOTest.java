package com.github.nexus.transaction;

import com.github.nexus.dao.JpaConfig;
import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.transaction.model.EncryptedTransaction;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EntityNotFoundException;

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
            = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getId());

        assertThat(retrieved).isNotNull().isEqualToComparingFieldByField(encryptedTransaction);

    }

    @Test
    public void fetchingAllTransactionsReturnsAll() {

        final List<EncryptedTransaction> payloads = IntStream.range(0, 50)
            .mapToObj(i -> new EncryptedTransaction(
                    new MessageHash(new byte[]{(byte) i}),
                    new byte[]{(byte) i}
                )
            )
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
        encryptedTransaction.setHash(new MessageHash(new byte[]{1}));
        encryptedTransactionDAO.save(encryptedTransaction);

        //check that it is actually in the database
        final EncryptedTransaction retrieved
            = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getId());
        assertThat(retrieved).isNotNull();

        //delete the transaction
        encryptedTransactionDAO.delete(new MessageHash(new byte[]{1}));

        //check it is not longer in the database
        final EncryptedTransaction deleted
            = entityManager.find(EncryptedTransaction.class, encryptedTransaction.getId());
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

}
