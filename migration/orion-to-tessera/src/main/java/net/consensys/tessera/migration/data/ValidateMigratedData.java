package net.consensys.tessera.migration.data;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class ValidateMigratedData {

    private EntityManagerFactory entityManagerFactory;

    public ValidateMigratedData(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public boolean validate(MigrationInfo migrationInfo) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        long txnCount = entityManager.createQuery("select count(t) from EncryptedTransaction t",Long.class).getSingleResult();
        long privacyGroupCount = entityManager.createQuery("select count(p) from PrivacyGroupEntity p",Long.class).getSingleResult();

        return txnCount == migrationInfo.getTransactionCount() && privacyGroupCount == migrationInfo.getPrivacyGroupCount();
    }

}
