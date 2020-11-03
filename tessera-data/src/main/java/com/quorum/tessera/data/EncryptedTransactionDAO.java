package com.quorum.tessera.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/** A data store for transactions that need to be retrieved later */
public interface EncryptedTransactionDAO {

    /**
     * Save a new Encrypted Transaction All fields are required to be non-null on the entity
     *
     * @param entity The entity to be persisted
     * @return The entity that was persisted
     */
    EncryptedTransaction save(EncryptedTransaction entity);

    /**
     * Update an existing Encrypted Transaction All fields are required to be non-null on the entity
     *
     * @param entity The entity to be merged
     * @return The entity that was merged
     */
    EncryptedTransaction update(EncryptedTransaction entity);

    /**
     * Retrieve a transaction based on its hash
     *
     * @param hash the hash of the transaction to retrieve
     * @return the encrypted transaction with the given hash
     */
    Optional<EncryptedTransaction> retrieveByHash(MessageHash hash);

    /**
     * Retrieve a list of transactions based on collection of hashes
     *
     * @param messageHashes the collection of hashes of the transactions to retrieve
     * @return A list of encrypted transactions
     */
    List<EncryptedTransaction> findByHashes(Collection<MessageHash> messageHashes);

    /**
     * Retrieves a list of transactions stored in the database
     *
     * @param offset the start offset
     * @param maxResult the maximum number of records to return
     * @return The list of requested rows from the database
     */
    List<EncryptedTransaction> retrieveTransactions(int offset, int maxResult);

    /**
     * Retrieve the total transaction count.
     *
     * @return the transaction count
     */
    long transactionCount();

    /**
     * Deletes a transaction that has the given hash as its digest
     *
     * @param hash The hash of the message to be deleted
     * @throws javax.persistence.EntityNotFoundException if there hash doesn't exist
     */
    void delete(MessageHash hash);

    /**
     * Perform a save of encrypted transaction with a callback
     *
     * @param transaction the entity to be persisted
     * @param consumer action that needs to be executed together
     * @return the entity that was persisted
     * @throws RuntimeException if the callback fails
     */
    <T> EncryptedTransaction save(EncryptedTransaction transaction, Callable<T> consumer);

    /**
     * Check whether data store is available
     *
     * @return true if data store is up and running ok, else false
     */
    boolean upcheck();
}
