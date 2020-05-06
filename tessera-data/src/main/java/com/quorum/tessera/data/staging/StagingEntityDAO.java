package com.quorum.tessera.data.staging;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A data store for transactions that need to be retrieved later */
public interface StagingEntityDAO {

    /**
     * Save a new Encrypted Transaction All fields are required to be non-null on the entity
     *
     * @param entity The entity to be persisted
     * @return The entity that was persisted
     */
    StagingTransaction save(StagingTransaction entity);

    /**
     * Update an Encrypted Transaction All fields are required to be non-null on the entity
     *
     * @param entity The entity to be updated
     * @return The entity that was updated
     */
    StagingTransaction update(StagingTransaction entity);


    default StagingTransaction saveOrUpdate(StagingTransaction stagingTransaction) {
        return Optional.of(stagingTransaction)
                .filter(s -> Objects.isNull(s.getId()))
                .map(this::save)
                .orElse(update(stagingTransaction));
    }

    /**
     * Retrieve a transaction based on its hash
     *
     * @param hash the hash of the transaction to retrieve
     * @return the encrypted transaction with the given hash
     */
    Optional<StagingTransaction> retrieveByHash(String hash);

    /**
     * Retrieves a list of all transactions stored in the database order by stage
     *
     * @return The list of all rows in the database order by stage
     */
    List<StagingTransaction> retrieveTransactionBatchOrderByStageAndHash(int offset, int maxResult);

    /**
     * counts all staging transactions
     *
     * @return the count
     */
    long countAll();

    /**
     * counts the staged transactions
     *
     * @return the count
     */
    long countStaged();


    /**
    * Find transactions ready to be staged and update validation stage.
     */
    int updateStageForBatch(int batchSize, long validationStage);

}
