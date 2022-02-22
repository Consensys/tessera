package com.quorum.tessera.data.internal;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerTemplate;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A JPA implementation of {@link EncryptedTransactionDAO} */
public class EncryptedTransactionDAOImpl implements EncryptedTransactionDAO {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionDAOImpl.class);

  private EntityManagerTemplate entityManagerTemplate;

  public EncryptedTransactionDAOImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
  }

  @Override
  public EncryptedTransaction save(final EncryptedTransaction entity) {
    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.persist(entity);
          LOGGER.debug("Stored transaction {}", entity.getHash());
          return entity;
        });
  }

  @Override
  public EncryptedTransaction update(final EncryptedTransaction entity) {
    return entityManagerTemplate.execute(
        entityManager -> {
          EncryptedTransaction existing =
              entityManager.find(EncryptedTransaction.class, entity.getHash());
          existing.setPayload(entity.getPayload());
          existing.setEncodedPayload(null);
          existing.setHash(entity.getHash());
          EncryptedTransaction merged = entityManager.merge(existing);
          LOGGER.debug("Updated transaction {}", entity.getHash());
          return merged;
        });
  }

  @Override
  public Optional<EncryptedTransaction> retrieveByHash(final MessageHash hash) {
    LOGGER.debug("Retrieving payload with hash {}", hash);
    return entityManagerTemplate.execute(
        entityManager ->
            entityManager
                .createNamedQuery("EncryptedTransaction.FindByHash", EncryptedTransaction.class)
                .setParameter("hash", hash.getHashBytes())
                .getResultStream()
                .findAny());
  }

  @Override
  public List<EncryptedTransaction> retrieveTransactions(int offset, int maxResult) {
    LOGGER.debug(
        "Fetching batch(offset:{},maxResult:{}) EncryptedTransaction database rows",
        offset,
        maxResult);
    return entityManagerTemplate.execute(
        entityManager ->
            entityManager
                .createNamedQuery("EncryptedTransaction.FindAll", EncryptedTransaction.class)
                .setFirstResult(offset)
                .setMaxResults(maxResult)
                .getResultList());
  }

  @Override
  public long transactionCount() {
    return entityManagerTemplate.execute(
        entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
          countQuery.select(criteriaBuilder.count(countQuery.from(EncryptedTransaction.class)));

          return entityManager.createQuery(countQuery).getSingleResult();
        });
  }

  @Override
  public void delete(final MessageHash hash) {

    LOGGER.info("Deleting transaction with hash {}", hash);

    entityManagerTemplate.execute(
        entityManager -> {
          final EncryptedTransaction message =
              entityManager
                  .createNamedQuery("EncryptedTransaction.FindByHash", EncryptedTransaction.class)
                  .setParameter("hash", hash.getHashBytes())
                  .getResultStream()
                  .findAny()
                  .orElseThrow(EntityNotFoundException::new);

          entityManager.remove(message);
          return message;
        });
  }

  @Override
  public void deleteAll(final PublicKey publicKey) {

    LOGGER.info("Deleting transaction with hash {}", publicKey);

    entityManagerTemplate.execute(
      entityManager -> {
        final List<EncryptedTransaction> messages =
          entityManager
            .createNamedQuery("EncryptedTransaction.FindAll", EncryptedTransaction.class)
            .getResultList();

        messages.stream().filter(msg-> publicKey.equals(msg.getPayload().getSenderKey()) || msg.getPayload().getRecipientKeys().contains(publicKey)).forEach(msg-> entityManager.remove(msg));
        return messages;
      });
  }

  @Override
  public <T> EncryptedTransaction save(EncryptedTransaction transaction, Callable<T> consumer) {

    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.persist(transaction);
          try {
            entityManager.flush();
            consumer.call();
            return transaction;
          } catch (RuntimeException ex) {
            throw ex;
          } catch (Exception e) {
            throw new PersistenceException(e);
          }
        });
  }

  @Override
  public boolean upcheck() {
    // if query succeeds then DB is up and running (else get exception)
    try {
      return entityManagerTemplate.execute(
          entityManager -> {
            Object result =
                entityManager.createNamedQuery("EncryptedTransaction.Upcheck").getSingleResult();

            return true;
          });
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public List<EncryptedTransaction> findByHashes(Collection<MessageHash> messageHashes) {
    if (Objects.isNull(messageHashes) || messageHashes.isEmpty()) {
      return Collections.EMPTY_LIST;
    }

    return entityManagerTemplate.execute(
        entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
          CriteriaQuery<EncryptedTransaction> query =
              criteriaBuilder.createQuery(EncryptedTransaction.class);

          Root<EncryptedTransaction> root = query.from(EncryptedTransaction.class);

          return entityManager
              .createQuery(query.select(root).where(root.get("hash").in(messageHashes)))
              .getResultList();
        });
  }
}
