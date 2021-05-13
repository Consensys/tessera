package net.consensys.tessera.migration.data;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class CountMigratedData {

  private EntityManagerFactory entityManagerFactory;

  public CountMigratedData(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  public Map<PayloadType, Long> countMigratedData() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    long txnCount =
        entityManager
            .createQuery("select count(t) from EncryptedTransaction t", Long.class)
            .getSingleResult();
    long privacyGroupCount =
        entityManager
            .createQuery("select count(p) from PrivacyGroupEntity p", Long.class)
            .getSingleResult();

    return Map.of(
        PayloadType.ENCRYPTED_PAYLOAD, txnCount,
        PayloadType.PRIVACY_GROUP_PAYLOAD, privacyGroupCount);
  }
}
