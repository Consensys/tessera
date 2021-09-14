package com.quorum.tessera.data.internal;

import com.quorum.tessera.data.EntityManagerTemplate;
import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.data.PrivacyGroupEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PrivacyGroupDAOImpl implements PrivacyGroupDAO {

  private EntityManagerTemplate entityManagerTemplate;

  public PrivacyGroupDAOImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
  }

  @Override
  public PrivacyGroupEntity save(PrivacyGroupEntity entity) {
    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.persist(entity);
          return entity;
        });
  }

  @Override
  public <T> PrivacyGroupEntity save(PrivacyGroupEntity entity, Callable<T> consumer) {
    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.persist(entity);
          try {
            entityManager.flush();
            consumer.call();
            return entity;
          } catch (RuntimeException ex) {
            throw ex;
          } catch (Exception e) {
            throw new PersistenceException(e);
          }
        });
  }

  @Override
  public PrivacyGroupEntity update(PrivacyGroupEntity entity) {
    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.merge(entity);
          return entity;
        });
  }

  @Override
  public <T> PrivacyGroupEntity update(PrivacyGroupEntity entity, Callable<T> consumer) {
    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.merge(entity);
          try {
            entityManager.flush();
            consumer.call();
            return entity;
          } catch (RuntimeException ex) {
            throw ex;
          } catch (Exception e) {
            throw new PersistenceException(e);
          }
        });
  }

  @Override
  public Optional<PrivacyGroupEntity> retrieve(byte[] id) {
    return entityManagerTemplate.execute(
        entityManager ->
            entityManager
                .createNamedQuery("PrivacyGroup.FindById", PrivacyGroupEntity.class)
                .setParameter("id", id)
                .getResultStream()
                .findAny());
  }

  @Override
  public PrivacyGroupEntity retrieveOrSave(PrivacyGroupEntity entity) {
    return entityManagerTemplate.retrieveOrSave(
        () -> retrieve(entity.getId()).orElse(null), () -> entity);
  }

  @Override
  public List<PrivacyGroupEntity> findByLookupId(byte[] lookupId) {
    return entityManagerTemplate.execute(
        entityManager ->
            entityManager
                .createNamedQuery("PrivacyGroup.FindByLookupId", PrivacyGroupEntity.class)
                .setParameter("lookupId", lookupId)
                .getResultStream()
                .collect(Collectors.toList()));
  }

  @Override
  public List<PrivacyGroupEntity> findAll() {
    return entityManagerTemplate.execute(
        em ->
            em.createNamedQuery("PrivacyGroup.FindAll", PrivacyGroupEntity.class).getResultList());
  }
}
