package com.quorum.tessera.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerTemplate.class);

  private final EntityManagerFactory entityManagerFactory;

  public EntityManagerTemplate(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
  }

  public <T> T execute(EntityManagerCallback<T> callback) {
    LOGGER.debug("Enter callback");
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();
    try {
      T outcome = callback.execute(entityManager);
      transaction.commit();
      LOGGER.debug("JPA callback success {}", outcome);
      return outcome;
    } catch (Exception ex) {
      LOGGER.warn("JPA exception thrown during execution {}", ex.getMessage());
      LOGGER.debug("", ex);
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      throw ex;
    } finally {
      entityManager.close();
    }
  }

  public <T> T retrieveOrSave(Supplier<T> retriever, Supplier<T> factory) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    return Optional.ofNullable(retriever.get())
        .orElseGet(
            () -> {
              try {
                transaction.begin();
                T result = factory.get();
                entityManager.persist(result);
                transaction.commit();
                return result;
              } catch (PersistenceException ex) {
                return Optional.ofNullable(retriever.get()).orElseThrow(() -> ex);
              } catch (Throwable throwable) {
                if (transaction.isActive()) transaction.rollback();
                throw throwable;
              } finally {
                entityManager.close();
              }
            });
  }
}
