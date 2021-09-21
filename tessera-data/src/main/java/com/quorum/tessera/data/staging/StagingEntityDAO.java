package com.quorum.tessera.data.staging;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

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
   *
   * @return number of records that have been updated
   */
  int updateStageForBatch(int batchSize, long validationStage);

  /**
   * counts all records in staging affected transactions
   *
   * @return the count
   */
  long countAllAffected();

  static StagingEntityDAO create() {
    return ServiceLoader.load(StagingEntityDAO.class).findFirst().get();
  }
}
