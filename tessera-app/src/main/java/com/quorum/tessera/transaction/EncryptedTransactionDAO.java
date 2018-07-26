package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.transaction.model.EncryptedTransaction;

import java.util.List;
import java.util.Optional;

/**
 * A data store for transactions that need to be retrieved later
 */
public interface EncryptedTransactionDAO {

    /**
     * Save a new Encrypted Transaction
     * All fields are required to be non-null on the entity
     *
     * @param entity The entity to be persisted
     * @return The entity that was persisted
     */
    EncryptedTransaction save(EncryptedTransaction entity);

    /**
     * Retrieve a transaction based on its hash
     *
     * @param hash the hash of the transaction to retrieve
     * @return the encrypted transaction with the given hash
     */
    Optional<EncryptedTransaction> retrieveByHash(MessageHash hash);

    /**
     * Retrieves a list of all transactions stored in the database
     *
     * @return The list of all rows in the database
     */
    List<EncryptedTransaction> retrieveAllTransactions();

    /**
     * Deletes a transaction that has the given hash as its digest
     *
     * @param hash The hash of the message to be deleted
     * @throws javax.persistence.EntityNotFoundException 
     */
    void delete(MessageHash hash);

}
