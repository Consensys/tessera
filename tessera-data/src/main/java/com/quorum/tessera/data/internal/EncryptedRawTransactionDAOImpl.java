package com.quorum.tessera.data.internal;

import com.quorum.tessera.data.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A JPA implementation of {@link EncryptedTransactionDAO} */
public class EncryptedRawTransactionDAOImpl implements EncryptedRawTransactionDAO {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(EncryptedRawTransactionDAOImpl.class);

  private final EntityManagerTemplate entityManagerTemplate;

  public EncryptedRawTransactionDAOImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
  }

  @Override
  public EncryptedRawTransaction save(final EncryptedRawTransaction entity) {
    LOGGER.debug(
        "Persisting EncryptedRawTransaction with hash {}, payload {}, key {}, nonce {} and from {}",
        entity.getHash(),
        toHexString(entity.getEncryptedPayload()),
        toHexString(entity.getEncryptedKey()),
        toHexString(entity.getNonce()),
        toHexString(entity.getSender()));

    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.persist(entity);
          return entity;
        });
  }

  @Override
  public Optional<EncryptedRawTransaction> retrieveByHash(final MessageHash hash) {
    LOGGER.debug("Retrieving payload with hash {}", hash);

    EncryptedRawTransaction encryptedRawTransaction =
        entityManagerTemplate.execute(
            entityManager -> entityManager.find(EncryptedRawTransaction.class, hash));

    return Optional.ofNullable(encryptedRawTransaction);
  }

  @Override
  public void delete(final MessageHash hash) {
    LOGGER.info("Deleting transaction with hash {}", hash);
    entityManagerTemplate.execute(
        entityManager -> {
          EncryptedRawTransaction txn = entityManager.find(EncryptedRawTransaction.class, hash);
          if (txn == null) {
            throw new EntityNotFoundException();
          }

          entityManager
              .createNamedQuery("EncryptedRawTransaction.DeleteByHash")
              .setParameter("hash", hash.getHashBytes())
              .executeUpdate();

          return txn;
        });
  }

  @Override
  public boolean upcheck() {
    // if query succeeds then DB is up and running (else get exception)
    try {
      return entityManagerTemplate.execute(
          entityManager -> {
            Object result =
                entityManager.createNamedQuery("EncryptedRawTransaction.Upcheck").getSingleResult();

            return true;
          });
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public long transactionCount() {
    upcheck();
    return entityManagerTemplate.execute(
        entityManager -> {
          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

          CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
          countQuery.select(criteriaBuilder.count(countQuery.from(EncryptedRawTransaction.class)));

          return entityManager.createQuery(countQuery).getSingleResult();
        });
  }

  @Override
  public List<EncryptedRawTransaction> retrieveTransactions(int offset, int maxResult) {
    LOGGER.debug(
        "Fetching batch(offset:{}, maxResult:{}) of EncryptedRawTransaction entries",
        offset,
        maxResult);
    return entityManagerTemplate.execute(
        entityManager ->
            entityManager
                .createNamedQuery("EncryptedRawTransaction.FindAll", EncryptedRawTransaction.class)
                .setFirstResult(offset)
                .setMaxResults(maxResult)
                .getResultList());
  }

  private String toHexString(byte[] val) {
    if (null == val) {
      return "null";
    }
    return Hex.toHexString(val);
  }
}
