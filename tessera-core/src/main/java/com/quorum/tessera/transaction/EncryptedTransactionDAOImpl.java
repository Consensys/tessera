package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;

/**
 * A JPA implementation of {@link EncryptedTransactionDAO}
 */
@Transactional
public class EncryptedTransactionDAOImpl implements EncryptedTransactionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionDAOImpl.class);

    private static final String FIND_HASH_EQUAL
        = "SELECT et FROM EncryptedTransaction et WHERE et.hash.hashBytes = :hash";

    private static final String FIND_ALL = "SELECT et FROM EncryptedTransaction et ORDER BY et.timestamp";

    @PersistenceContext(unitName = "tessera")
    private EntityManager entityManager;

    @Override
    public EncryptedTransaction save(final EncryptedTransaction entity) {
        entityManager.persist(entity);
        LOGGER.info("Stored transaction {}",entity.getHash());
        return entity;
    }

    @Override
    public Optional<EncryptedTransaction> retrieveByHash(final MessageHash hash) {
        LOGGER.info("Retrieving payload with hash {}", hash);

        return entityManager
            .createQuery(FIND_HASH_EQUAL, EncryptedTransaction.class)
            .setParameter("hash", hash.getHashBytes())
            .getResultStream()
            .findAny();
    }

    @Override
    public List<EncryptedTransaction> retrieveTransactions(int offset, int maxResult) {
        LOGGER.info("Fetching batch(offset:{},maxResult:{}) of EncryptedTransaction database rows", offset, maxResult);

        return entityManager
            .createQuery(FIND_ALL, EncryptedTransaction.class)
            .setFirstResult(offset)
            .setMaxResults(maxResult)
            .getResultList();
    }

    @Override
    public long transactionCount() {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        countQuery.select(criteriaBuilder.count(countQuery.from(EncryptedTransaction.class)));

        return entityManager.createQuery(countQuery)
            .getSingleResult();

    }

    @Override
    public void delete(final MessageHash hash) {
        LOGGER.info("Deleting transaction with hash {}", hash);

        final EncryptedTransaction message = entityManager
            .createQuery(FIND_HASH_EQUAL, EncryptedTransaction.class)
            .setParameter("hash", hash.getHashBytes())
            .getResultStream()
            .findAny()
            .orElseThrow(EntityNotFoundException::new);

        entityManager.remove(message);
    }

}
