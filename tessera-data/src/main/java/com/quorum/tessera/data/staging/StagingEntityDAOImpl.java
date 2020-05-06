package com.quorum.tessera.data.staging;

import com.quorum.tessera.data.EntityManagerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A JPA implementation of {@link StagingEntityDAO}
 */
public class StagingEntityDAOImpl implements StagingEntityDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEntityDAOImpl.class);

    private EntityManagerTemplate entityManagerTemplate;

    public StagingEntityDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
    }

    @Override
    public StagingTransaction save(final StagingTransaction entity) {
        return entityManagerTemplate.execute(entityManager -> {
            entityManager.persist(entity);

            LOGGER.debug("Persisting StagingTransaction entity with hash {} ", entity.getHash());

            return entity;
        });
    }

    @Override
    public StagingTransaction update(StagingTransaction entity) {

        return entityManagerTemplate.execute(entityManager -> {
            entityManager.merge(entity);

            LOGGER.debug("Merging StagingTransaction entity with hash {}", entity.getHash());

            return entity;
        });


    }

    @Override
    public Optional<StagingTransaction> retrieveByHash(final String hash) {
        return entityManagerTemplate.execute(entityManager -> {
            LOGGER.info("Retrieving payload with hash {}", hash);

            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<StagingTransaction> query = criteriaBuilder.createQuery(StagingTransaction.class);
            Root<StagingTransaction> root = query.from(StagingTransaction.class);

            query.select(root)
                .where(
                    criteriaBuilder.equal(root.get("hash"),hash)
                );
            return Optional.ofNullable(entityManager.createQuery(query).getSingleResult());
        });
    }

    @Override
    public List<StagingTransaction> retrieveTransactionBatchOrderByStageAndHash(int offset, int maxResults) {
        LOGGER.debug(
            "Fetching batch (offset:{},maxResults:{}) of StagingTransaction database rows order by stage and hash",
            offset,
            maxResults);

        return entityManagerTemplate.execute(em -> em
            .createNamedQuery("StagingTransaction.findAllOrderByStage", StagingTransaction.class)
            .setFirstResult(offset)
            .setMaxResults(maxResults)
            .getResultList());
    }


    @Override
    public long countAll() {
        return entityManagerTemplate.execute(em ->
            em.createNamedQuery("StagingTransaction.countAll", Long.class).getSingleResult()
        );
    }

    @Override
    public long countStaged() {
        return entityManagerTemplate.execute(em ->
            em.createNamedQuery("StagingTransaction.countStaged", Long.class).getSingleResult()
        );
    }

    @Override
    public void performStaging(int batchSize) {

        AtomicLong stage = new AtomicLong(0);

        while (true) {
            final long stg = stage.incrementAndGet();
            boolean outcome = entityManagerTemplate.execute(entityManager -> {

                List<StagingTransaction> resultList = entityManager.createNamedQuery("StagingTransaction.stagingQuery", StagingTransaction.class)
                    .setMaxResults(batchSize)
                    .getResultList();

                for (StagingTransaction st : resultList) {
                    st.setValidationStage(stg);
                }
                return resultList.isEmpty();
            });
            if(outcome) {
                break;
            }
        }
    }
}
