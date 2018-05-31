package com.github.nexus.dao;

import com.github.nexus.entity.SomeEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class SomeDAOImpl implements SomeDAO {

    @PersistenceContext(unitName = "nexus")
    private EntityManager entityManager;



    @Override
    public void save(SomeEntity entity) {
       entityManager.persist(entity);
    }


}
