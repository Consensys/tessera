package com.github.tessera.transaction;

import com.github.tessera.enclave.model.MessageHash;
import com.github.tessera.transaction.model.EncryptedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EncryptedTransactionDAOImpl implements EncryptedTransactionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionDAOImpl.class);

    @PersistenceContext(unitName = "tessera")
    private EntityManager entityManager;

    @Override
    public EncryptedTransaction save(final EncryptedTransaction entity) {
        entityManager.persist(entity);

        LOGGER.debug("Persisting entity with ID {}, hash {} and payload {}",
            entity.getId(), Arrays.toString(entity.getHash()), Arrays.toString(entity.getEncodedPayload())
        );

        return entity;
    }

    @Override
    public Optional<EncryptedTransaction> retrieveByHash(final MessageHash hash) {
        final String query = "SELECT et FROM EncryptedTransaction et WHERE et.hash = :hash";

        return entityManager
            .createQuery(query, EncryptedTransaction.class)
            .setParameter("hash", hash.getHashBytes())
            .getResultStream()
            .findAny();
    }

    @Override
    public List<EncryptedTransaction> retrieveAllTransactions() {
        LOGGER.debug("Fetching all EncryptedTransaction database rows");

        return entityManager
            .createQuery("SELECT et FROM EncryptedTransaction et", EncryptedTransaction.class)
            .getResultList();
    }
    
    
    @Override
    public void delete(final MessageHash hash) {
        final String query = "select et from EncryptedTransaction et where et.hash = :hash";

        final EncryptedTransaction message = entityManager
            .createQuery(query, EncryptedTransaction.class)
            .setParameter("hash", hash.getHashBytes())
            .getResultStream()
            .findAny()
                .orElseThrow(EntityNotFoundException::new);

        entityManager.remove(message);


    }
}
