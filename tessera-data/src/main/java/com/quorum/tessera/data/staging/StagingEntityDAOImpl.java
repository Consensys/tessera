package com.quorum.tessera.data.staging;

import com.quorum.tessera.data.EntityManagerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A JPA implementation of {@link StagingEntityDAO}
 */

public class StagingEntityDAOImpl implements StagingEntityDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEntityDAOImpl.class);

    private static final String FIND_ALL_ORDER_BY_STAGE =
        "SELECT st FROM StagingTransaction st ORDER BY "
            + "COALESCE(st.validationStage, SELECT max(st.validationStage)+1 FROM StagingTransaction st), st.hash";

    private static final String COUNT_ALL = "SELECT count(st) from StagingTransaction st";

    private static final String COUNT_STAGED =
        "SELECT count(st) from StagingTransaction st where st.validationStage is not null";


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
    public Optional<StagingTransaction> retrieveByHash(final MessageHashStr hash) {
        return entityManagerTemplate.execute(entityManager -> {
            LOGGER.info("Retrieving payload with hash {}", hash);
            return Optional.ofNullable(entityManager.find(StagingTransaction.class, hash));
        });
    }

    @Override
    public List<StagingTransaction> retrieveTransactionBatchOrderByStageAndHash(int offset, int maxResults) {
        LOGGER.info(
            "Fetching batch (offset:{},maxResults:{}) of StagingTransaction database rows order by stage and hash",
            offset,
            maxResults);

        return entityManagerTemplate.execute(entityManager -> {
            return entityManager
                .createQuery(FIND_ALL_ORDER_BY_STAGE, StagingTransaction.class)
                .setFirstResult(offset)
                .setMaxResults(maxResults)
                .getResultList();
        });
    }


    @Override
    public long countAll() {

        return entityManagerTemplate.execute(entityManager -> {
            return entityManager.createQuery(COUNT_ALL, Long.class).getSingleResult();
        });
    }

    @Override
    public long countStaged() {
        return entityManagerTemplate.execute(entityManager -> {
            return entityManager.createQuery(COUNT_STAGED, Long.class).getSingleResult();
        });
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
