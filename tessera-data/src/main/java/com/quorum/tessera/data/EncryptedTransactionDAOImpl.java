package com.quorum.tessera.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import java.util.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/** A JPA implementation of {@link EncryptedTransactionDAO} */
public class EncryptedTransactionDAOImpl implements EncryptedTransactionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionDAOImpl.class);

    private EntityManagerTemplate entityManagerTemplate;

    public EncryptedTransactionDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
    }

    @Override
    public EncryptedTransaction save(final EncryptedTransaction entity) {
        return entityManagerTemplate.execute(entityManager -> {
            entityManager.persist(entity);
            LOGGER.debug("Stored transaction {}", entity.getHash());
            return entity;
        });
    }

    @Override
    public Optional<EncryptedTransaction> retrieveByHash(final MessageHash hash) {
        LOGGER.debug("Retrieving payload with hash {}", hash);
        return entityManagerTemplate.execute(entityManager -> entityManager
            .createNamedQuery("EncryptedTransaction.FindByHash", EncryptedTransaction.class)
            .setParameter("hash", hash.getHashBytes())
            .getResultStream()
            .findAny());
    }

    @Override
    public List<EncryptedTransaction> retrieveTransactions(int offset, int maxResult) {
        LOGGER.debug("Fetching batch(offset:{},maxResult:{}) EncryptedTransaction database rows", offset, maxResult);
        return entityManagerTemplate.execute(entityManager ->
                entityManager.createNamedQuery("EncryptedTransaction.FindAll", EncryptedTransaction.class)
                                .setFirstResult(offset)
                                .setMaxResults(maxResult)
                                .getResultList());
    }

    @Override
    public long transactionCount() {
        return entityManagerTemplate.execute(entityManager -> {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

            CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
            countQuery.select(criteriaBuilder.count(countQuery.from(EncryptedTransaction.class)));

            return entityManager.createQuery(countQuery).getSingleResult();
        });

    }

    @Override
    public void delete(final MessageHash hash) {

        LOGGER.info("Deleting transaction with hash {}", hash);

        entityManagerTemplate.execute(entityManager -> {
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
    public List<EncryptedTransaction> findByHashes(Collection<MessageHash> messageHashes) {
        if(Objects.isNull(messageHashes) || messageHashes.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return entityManagerTemplate.execute(entityManager -> {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<EncryptedTransaction> query =
                criteriaBuilder.createQuery(EncryptedTransaction.class);

            Root<EncryptedTransaction> root = query.from(EncryptedTransaction.class);

            return entityManager.createQuery(
                query.select(root)
                    .where(root.get("hash")
                        .in(messageHashes))
                ).getResultList();
        });
    }
}
