package com.github.nexus.dao;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.entity.EncryptedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EncryptedTransactionDAOImpl implements EncryptedTransactionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionDAOImpl.class);

    @PersistenceContext(unitName = "nexus")
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
    public List<EncryptedTransaction> retrieveAllTransactions() {
        LOGGER.debug("Fetching all EncryptedTransaction database rows");

        return entityManager
            .createQuery("SELECT et FROM EncryptedTransaction et", EncryptedTransaction.class)
            .getResultList();
    }

    @Override
    public boolean delete(final MessageHash hash) {
        final String query = "SELECT et FROM EncryptedTransaction et WHERE et.hash = :hash";

        final Optional<EncryptedTransaction> message = entityManager
            .createQuery(query, EncryptedTransaction.class)
            .setParameter("hash", hash.getHashBytes())
            .getResultList()
            .stream()
            .findAny();

        if(message.isPresent()) {
            entityManager.remove(message.get());
            return true;
        } else {
            return false;
        }

    }
}
