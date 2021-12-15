package com.quorum.tessera.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

public interface EntityManagerCallback<T> {

  T execute(EntityManager entityManager) throws PersistenceException;
}
