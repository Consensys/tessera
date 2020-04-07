package com.quorum.tessera.data.staging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/** A JPA implementation of {@link StagingEntityDAO} */
@Transactional
public class StagingEntityDAOImpl implements StagingEntityDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEntityDAOImpl.class);

    private static final String FIND_HASH_EQUAL = "SELECT st FROM StagingTransaction st WHERE st.hash.hash = :hash";

    private static final String FIND_ALL_ORDER_BY_STAGE =
            "SELECT st FROM StagingTransaction st ORDER BY "
                    + "COALESCE(st.validationStage, SELECT max(st.validationStage)+1 FROM StagingTransaction st), st.hash";

    private static final String COUNT_ALL = "SELECT count(st) from StagingTransaction st";

    private static final String COUNT_STAGED =
            "SELECT count(st) from StagingTransaction st where st.validationStage is not null";

    @PersistenceContext(unitName = "tessera")
    private EntityManager entityManager;

    private final StagingEntityDAOBatch stagingEntityDAOBatch;

    public StagingEntityDAOImpl(StagingEntityDAOBatch stagingEntityDAOBatch) {
        this.stagingEntityDAOBatch = stagingEntityDAOBatch;
    }

    @Override
    public StagingTransaction save(final StagingTransaction entity) {
        entityManager.persist(entity);

        LOGGER.debug("Persisting StagingTransaction entity with hash {} ", entity.getHash());

        return entity;
    }

    @Override
    public StagingTransaction update(StagingTransaction entity) {
        entityManager.merge(entity);

        LOGGER.debug("Merging StagingTransaction entity with hash {}", entity.getHash());

        return entity;
    }

    @Override
    public Optional<StagingTransaction> retrieveByHash(final MessageHashStr hash) {
        LOGGER.info("Retrieving payload with hash {}", hash);
        return Optional.ofNullable(entityManager.find(StagingTransaction.class, hash));
    }

    @Override
    public List<StagingTransaction> retrieveTransactionBatchOrderByStageAndHash(int offset, int maxResults) {
        LOGGER.info(
                "Fetching batch (offset:{},maxResults:{}) of StagingTransaction database rows order by stage and hash",
                offset,
                maxResults);

        return entityManager
                .createQuery(FIND_ALL_ORDER_BY_STAGE, StagingTransaction.class)
                .setFirstResult(offset)
                .setMaxResults(maxResults)
                .getResultList();
    }

    @Override
    public void delete(final MessageHashStr hash) {
        LOGGER.info("Deleting transaction with hash {}", hash);

        final StagingTransaction message =
                entityManager
                        .createQuery(FIND_HASH_EQUAL, StagingTransaction.class)
                        .setParameter("hash", hash.getHash())
                        .getResultStream()
                        .findAny()
                        .orElseThrow(EntityNotFoundException::new);

        entityManager.remove(message);
    }

    @Override
    public void cleanStagingArea(int batchSize) {
        int count = 0;
        while ((count = stagingEntityDAOBatch.deleteBatch(batchSize)) > 0) {
            LOGGER.info("Deleted StagingTransaction batch of {} records", count);
        }
    }

    @Override
    public long countAll() {
        return entityManager.createQuery(COUNT_ALL, Long.class).getSingleResult();
    }

    @Override
    public long countStaged() {
        return entityManager.createQuery(COUNT_STAGED, Long.class).getSingleResult();
    }

    @Override
    public void performStaging(int batchSize) {
        boolean shouldStop = false;
        int stage = 0;

        while (!shouldStop) {
            stage++;
            shouldStop = stagingEntityDAOBatch.assignValidationStageToBatch(stage, batchSize) == 0;
        }
    }
}
