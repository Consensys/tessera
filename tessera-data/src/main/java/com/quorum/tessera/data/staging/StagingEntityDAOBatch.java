package com.quorum.tessera.data.staging;

/** A data store for transactions that need to be retrieved later */
public interface StagingEntityDAOBatch {

    /**
     * Perform staging for received transactions in staging database. Perform query to update validation stage until all
     * records have been validated
     *
     * @return number of records that were assigned the validation stage
     */
    int assignValidationStageToBatch(int validationStage, int batchSize);

    /** Perform query to clear a batch of transactions */
    int deleteBatch(int batchSize);
}
