package com.quorum.tessera.data.internal;

import com.quorum.tessera.data.EncryptedMessage;
import com.quorum.tessera.data.EncryptedMessageDAO;
import com.quorum.tessera.data.EntityManagerTemplate;
import com.quorum.tessera.data.MessageHash;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A JPA implementation of {@link EncryptedMessageDAO} */
public class EncryptedMessageDAOImpl implements EncryptedMessageDAO {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedMessageDAOImpl.class);

  private EntityManagerTemplate entityManagerTemplate;

  public EncryptedMessageDAOImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
  }

  @Override
  public EncryptedMessage save(final EncryptedMessage entity) {
    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.persist(entity);
          LOGGER.debug("Stored message {}", entity.getHash());
          return entity;
        });
  }

  @Override
  public Optional<EncryptedMessage> retrieveByHash(final MessageHash hash) {
    LOGGER.debug("Retrieving message with hash {}", hash);
    return entityManagerTemplate.execute(
        entityManager ->
            entityManager
                .createNamedQuery("EncryptedMessage.FindByHash", EncryptedMessage.class)
                .setParameter("hash", hash.getHashBytes())
                .getResultStream()
                .findAny());
  }

  @Override
  public List<MessageHash> retrieveMessageHashes(int offset, int maxResult) {
    LOGGER.debug(
        "Fetching batch(offset:{},maxResult:{}) EncryptedMessage database rows", offset, maxResult);
    return entityManagerTemplate.execute(
        entityManager ->
            entityManager
                .createNamedQuery("EncryptedMessage.FindAllHashes", MessageHash.class)
                .setFirstResult(offset)
                .setMaxResults(maxResult)
                .getResultList());
  }

  @Override
  public long messageCount() {
    return entityManagerTemplate.execute(
        entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
          countQuery.select(criteriaBuilder.count(countQuery.from(EncryptedMessage.class)));

          return entityManager.createQuery(countQuery).getSingleResult();
        });
  }

  @Override
  public void delete(final MessageHash hash) {

    LOGGER.info("Deleting message with hash {}", hash);

    entityManagerTemplate.execute(
        entityManager -> {
          final EncryptedMessage message =
              entityManager
                  .createNamedQuery("EncryptedMessage.FindByHash", EncryptedMessage.class)
                  .setParameter("hash", hash.getHashBytes())
                  .getResultStream()
                  .findAny()
                  .orElseThrow(EntityNotFoundException::new);

          entityManager.remove(message);
          return message;
        });
  }

  @Override
  public boolean upcheck() {
    // if query succeeds then DB is up and running (else get exception)
    try {
      return entityManagerTemplate.execute(
          entityManager -> {
            Object result =
                entityManager.createNamedQuery("EncryptedMessage.Upcheck").getSingleResult();

            return true;
          });
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public List<EncryptedMessage> findByHashes(Collection<MessageHash> messageHashes) {
    if (Objects.isNull(messageHashes) || messageHashes.isEmpty()) {
      return Collections.emptyList();
    }

    return entityManagerTemplate.execute(
        entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
          CriteriaQuery<EncryptedMessage> query =
              criteriaBuilder.createQuery(EncryptedMessage.class);

          Root<EncryptedMessage> root = query.from(EncryptedMessage.class);

          return entityManager
              .createQuery(query.select(root).where(root.get("hash").in(messageHashes)))
              .getResultList();
        });
  }
}
