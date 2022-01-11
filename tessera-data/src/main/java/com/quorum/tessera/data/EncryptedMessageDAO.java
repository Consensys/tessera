package com.quorum.tessera.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/** A data store for messages that need to be retrieved later */
public interface EncryptedMessageDAO {

  /**
   * Save a new Encrypted Message All fields are required to be non-null on the entity
   *
   * @param entity The entity to be persisted
   * @return The entity that was persisted
   */
  EncryptedMessage save(EncryptedMessage entity);

  /**
   * Retrieve a message based on its hash
   *
   * @param hash the hash of the message to retrieve
   * @return the encrypted message with the given hash
   */
  Optional<EncryptedMessage> retrieveByHash(MessageHash hash);

  /**
   * Retrieve a list of transactions based on collection of hashes
   *
   * @param messageHashes the collection of hashes of the transactions to retrieve
   * @return A list of encrypted transactions
   */
  List<EncryptedMessage> findByHashes(Collection<MessageHash> messageHashes);

  /**
   * Retrieves a list of transactions stored in the database
   *
   * @param offset the start offset
   * @param maxResult the maximum number of records to return
   * @return The list of requested rows from the database
   */
  List<MessageHash> retrieveMessageHashes(int offset, int maxResult);

  /**
   * Retrieve the total message count.
   *
   * @return the message count
   */
  long messageCount();

  /**
   * Deletes a message that has the given hash as its digest
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

  static EncryptedMessageDAO create() {
    return ServiceLoader.load(EncryptedMessageDAO.class).findFirst().get();
  }
}
