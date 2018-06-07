package com.github.nexus.transaction;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.transaction.model.EncryptedTransaction;

import java.util.List;

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
     * Retrieves a list of all transactions stored in the database
     *
     * @return The list of all rows in the database
     */
    List<EncryptedTransaction> retrieveAllTransactions();

    /**
     * Deletes a transaction that has the given hash as its digest
     *
     * @param hash The hash of the message to be deleted
     * @return whether an entity was deleted
     */
    boolean delete(MessageHash hash);

}
