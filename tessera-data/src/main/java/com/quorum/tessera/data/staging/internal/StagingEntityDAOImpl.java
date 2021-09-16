package com.quorum.tessera.data.staging.internal;

import com.quorum.tessera.data.EntityManagerTemplate;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A JPA implementation of {@link StagingEntityDAO} */
public class StagingEntityDAOImpl implements StagingEntityDAO {

  private static final Logger LOGGER = LoggerFactory.getLogger(StagingEntityDAOImpl.class);

  private EntityManagerTemplate entityManagerTemplate;

  public StagingEntityDAOImpl(EntityManagerFactory entityManagerFactory) {
    this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
  }

  @Override
  public StagingTransaction save(final StagingTransaction entity) {
    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.persist(entity);

          LOGGER.debug("Persisting StagingTransaction entity with hash {} ", entity.getHash());

          return entity;
        });
  }

  @Override
  public StagingTransaction update(StagingTransaction entity) {

    return entityManagerTemplate.execute(
        entityManager -> {
          entityManager.merge(entity);

          LOGGER.debug("Merging StagingTransaction entity with hash {}", entity.getHash());

          return entity;
        });
  }

  @Override
  public Optional<StagingTransaction> retrieveByHash(final String hash) {
    return entityManagerTemplate.execute(
        entityManager -> {
          LOGGER.debug("Retrieving payload with hash {}", hash);

          CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
          CriteriaQuery<StagingTransaction> query =
              criteriaBuilder.createQuery(StagingTransaction.class);
          Root<StagingTransaction> root = query.from(StagingTransaction.class);
          query.select(root).where(criteriaBuilder.equal(root.get("hash"), hash));
          return Optional.ofNullable(entityManager.createQuery(query).getSingleResult());
        });
  }

  @Override
  public List<StagingTransaction> retrieveTransactionBatchOrderByStageAndHash(
      int offset, int maxResults) {
    LOGGER.debug(
        "Fetching batch (offset:{},maxResults:{}) of StagingTransaction database rows order by stage and hash",
        offset,
        maxResults);

    return entityManagerTemplate.execute(
        em ->
            em.createNamedQuery("StagingTransaction.findAllOrderByStage", StagingTransaction.class)
                .setFirstResult(offset)
                .setMaxResults(maxResults)
                .getResultList());
  }

  @Override
  public long countAll() {
    return entityManagerTemplate.execute(
        em -> em.createNamedQuery("StagingTransaction.countAll", Long.class).getSingleResult());
  }

  @Override
  public long countStaged() {
    return entityManagerTemplate.execute(
        em -> em.createNamedQuery("StagingTransaction.countStaged", Long.class).getSingleResult());
  }

  @Override
  public int updateStageForBatch(int batchSize, long validationStage) {

    return entityManagerTemplate.execute(
        entityManager -> {
          List<StagingTransaction> resultList =
              entityManager
                  .createNamedQuery("StagingTransaction.stagingQuery", StagingTransaction.class)
                  .setMaxResults(batchSize)
                  .getResultList();

          resultList.forEach(st -> st.setValidationStage(validationStage));

          return resultList.size();
        });
  }

  @Override
  public long countAllAffected() {
    return entityManagerTemplate.execute(
        em ->
            em.createNamedQuery("StagingAffectedTransaction.countAll", Long.class)
                .getSingleResult());
  }
}
