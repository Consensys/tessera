package com.github.nexus.dao;

import com.github.nexus.entity.SomeEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class SomeDAOImpl implements SomeDAO {

    private static EntityManagerFactory entityManagerFactory;

    public static EntityManager getEntityManager(){
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public void save(SomeEntity entity) {
       getEntityManager().persist(entity);
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
}
