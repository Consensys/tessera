package com.quorum.tessera.data;


import java.util.List;
import java.util.Optional;

/**
 * A data store for transactions that need to be retrieved later
 */
public interface StagingEntityDAO {

    /**
     * Save a new Encrypted Transaction
     * All fields are required to be non-null on the entity
     *
     * @param entity The entity to be persisted
     * @return The entity that was persisted
     */
    StagingTransaction save(StagingTransaction entity);

    /**
     * Update an Encrypted Transaction
     * All fields are required to be non-null on the entity
     *
     * @param entity The entity to be updated
     * @return The entity that was updated
     */
    StagingTransaction update(StagingTransaction entity);

    /**
     * Retrieve a transaction based on its hash
     *
     * @param hash the hash of the transaction to retrieve
     * @return the encrypted transaction with the given hash
     */
    Optional<StagingTransaction> retrieveByHash(MessageHashStr hash);

    /**
     * Retrieves a list of all transactions stored in the database order by stage
     *
     * @return The list of all rows in the database order by stage
     */
    List<StagingTransaction> retrieveTransactionBatchOrderByStageAndHash(int offset, int maxResult);

    /**
     * Deletes a transaction that has the given hash as its digest
     *
     * @param hash The hash of the message to be deleted
     * @throws javax.persistence.EntityNotFoundException when message is not found
     */
    void delete(MessageHashStr hash);

    /**
     * Perform staging for received transactions in staging database.
     * Perform query to update validation stage until all records have been validated
     */
    void performStaging(int batchSize);

    /**
     * Perform query to clear data currently existed in staging table
     */
    void cleanStagingArea(int batchSize);

    /**
     * counts all staging transactions
     * @return the count
     */
    long countAll();

    /**
     * counts the staged transactions
     * @return the count
     */
    long countStaged();

}
