package com.quorum.tessera.data;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public interface EntityManagerCallback<T> {

    T execute(EntityManager entityManager) throws PersistenceException;
}
