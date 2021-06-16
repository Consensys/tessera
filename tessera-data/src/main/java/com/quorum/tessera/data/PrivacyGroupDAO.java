package com.quorum.tessera.data;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

/** A data store for privacy group data that need to be retrieved later */
public interface PrivacyGroupDAO {

  /**
   * Save a new privacy group entity to the database.
   *
   * @param entity The entity to be persisted
   * @return Persisted entity
   */
  PrivacyGroupEntity save(PrivacyGroupEntity entity);

  /**
   * Save a new privacy group entity to database with a callback
   *
   * @param entity The entity to be persisted
   * @param consumer Action that needs to be executed together
   * @return The entity that was persisted
   * @throws RuntimeException if the callback fails
   */
  <T> PrivacyGroupEntity save(PrivacyGroupEntity entity, Callable<T> consumer);

  /**
   * Update an existing privacy group entity to the database.
   *
   * @param entity The entity to be merged
   * @return Merged entity
   */
  PrivacyGroupEntity update(PrivacyGroupEntity entity);

  /**
   * Update an existing privacy group entity to database with a callback
   *
   * @param entity The entity to be merged
   * @param consumer Action that needs to be executed together
   * @return The entity that was merged
   * @throws RuntimeException if the callback fails
   */
  <T> PrivacyGroupEntity update(PrivacyGroupEntity entity, Callable<T> consumer);

  /**
   * Retrieve privacy group entity from database based on its privacy group id
   *
   * @param id The privacy group id
   * @return PrivacyGroup entity contains encoded PrivacyGroup data
   */
  Optional<PrivacyGroupEntity> retrieve(byte[] id);

  /**
   * Retrieve privacy group entity from database based on id If not already exists will persist
   * entity to database
   *
   * @param entity
   * @return persisted or retrieved entity
   */
  PrivacyGroupEntity retrieveOrSave(PrivacyGroupEntity entity);

  /**
   * Retrieve matching privacy groups based on its lookup id
   *
   * @param lookupId The lookup id used to find privacy groups
   * @return A list of privacy group entities
   */
  List<PrivacyGroupEntity> findByLookupId(byte[] lookupId);

  List<PrivacyGroupEntity> findAll();

  static PrivacyGroupDAO create() {
    return ServiceLoader.load(PrivacyGroupDAO.class).findFirst().get();
  }
}
