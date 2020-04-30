package com.quorum.tessera.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/** A JPA implementation of {@link EncryptedTransactionDAO} */
public class EncryptedTransactionDAOImpl implements EncryptedTransactionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionDAOImpl.class);

    private static final String FIND_HASH_EQUAL =
            "SELECT et FROM EncryptedTransaction et WHERE et.hash.hashBytes = :hash";

    private static final String FIND_ALL = "SELECT et FROM EncryptedTransaction et ORDER BY et.timestamp,et.hash";


    private EntityManagerTemplate entityManagerTemplate;

    public EncryptedTransactionDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
    }

    @Override
    public EncryptedTransaction save(final EncryptedTransaction entity) {
        return entityManagerTemplate.execute(entityManager -> {
            entityManager.persist(entity);
            LOGGER.info("Stored transaction {}", entity.getHash());
            return entity;
        });
    }

    @Override
    public Optional<EncryptedTransaction> retrieveByHash(final MessageHash hash) {
        LOGGER.info("Retrieving payload with hash {}", hash);
        return entityManagerTemplate.execute(entityManager -> entityManager
            .createQuery(FIND_HASH_EQUAL, EncryptedTransaction.class)
            .setParameter("hash", hash.getHashBytes())
            .getResultStream()
            .findAny());
    }

    @Override
    public List<EncryptedTransaction> retrieveTransactions(int offset, int maxResult) {
        LOGGER.info("Fetching batch(offset:{},maxResult:{}) EncryptedTransaction database rows", offset, maxResult);
        return entityManagerTemplate.execute(entityManager -> entityManager.createQuery(FIND_ALL, EncryptedTransaction.class)
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
                    .createQuery(FIND_HASH_EQUAL, EncryptedTransaction.class)
                    .setParameter("hash", hash.getHashBytes())
                    .getResultStream()
                    .findAny()
                    .orElseThrow(EntityNotFoundException::new);

            entityManager.remove(message);
            return message;
        });


    }
}
