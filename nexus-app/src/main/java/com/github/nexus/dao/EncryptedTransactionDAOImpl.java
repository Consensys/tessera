package com.github.nexus.dao;

import com.github.nexus.entity.EncryptedTransaction;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class EncryptedTransactionDAOImpl implements EncryptedTransactionDAO {

    @PersistenceContext(unitName = "nexus")
    private EntityManager entityManager;

    @Override
    public EncryptedTransaction save(final EncryptedTransaction entity) {
       entityManager.persist(entity);
       return entity;
    }

    @Override
    public List<EncryptedTransaction> retrieveAllTransactions() {
        return entityManager
            .createQuery("SELECT et FROM EncryptedTransaction et", EncryptedTransaction.class)
            .getResultList();
    }

}
