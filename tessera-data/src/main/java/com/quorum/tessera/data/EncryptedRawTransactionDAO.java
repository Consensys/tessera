package com.quorum.tessera.data;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/** A data store for transactions that need to be retrieved later */
public interface EncryptedRawTransactionDAO {

  /**
   * Save a new Encrypted Transaction All fields are required to be non-null on the entity
   *
   * @param entity The entity to be persisted
   * @return The entity that was persisted
   */
  EncryptedRawTransaction save(EncryptedRawTransaction entity);

  /**
   * Retrieve a transaction based on its hash
   *
   * @param hash the hash of the transaction to retrieve
   * @return the encrypted transaction with the given hash
   */
  Optional<EncryptedRawTransaction> retrieveByHash(MessageHash hash);

  /**
   * Deletes a transaction that has the given hash as its digest
   *
   * @param hash The hash of the message to be deleted
   * @throws jakarta.persistence.EntityNotFoundException if there hash doesn't exist
   */
  void delete(MessageHash hash);

  /**
   * Check whether data store is available
   *
   * @return true if data store is up and running ok, else false
   */
  boolean upcheck();

  /**
   * Retrieve the total transaction count.
   *
   * @return the transaction count
   */
  long transactionCount();

  /**
   * Retrieves a list of transactions stored in the database
   *
   * @param offset the start offset
   * @param maxResult the maximum number of records to return
   * @return The list of requested rows from the database
   */
  List<EncryptedRawTransaction> retrieveTransactions(int offset, int maxResult);

  static EncryptedRawTransactionDAO create() {
    return ServiceLoader.load(EncryptedRawTransactionDAO.class).findFirst().get();
  }
}
