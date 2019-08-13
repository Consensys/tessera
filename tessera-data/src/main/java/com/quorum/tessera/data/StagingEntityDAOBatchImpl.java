package com.quorum.tessera.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

public class StagingEntityDAOBatchImpl implements StagingEntityDAOBatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(StagingEntityDAOBatchImpl.class);

    private static final String FIND_ALL = "SELECT st FROM StagingTransaction st";

    private static final String STAGING_QUERY = "StagingTransaction.stagingQuery";

    @PersistenceContext(unitName = "tessera")
    private EntityManager entityManager;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int assignValidationStageToBatch(int validationStage, int batchSize) {
        List<StagingTransaction> resultList = entityManager
            .createNamedQuery(STAGING_QUERY, StagingTransaction.class)
            .setMaxResults(batchSize)
            .getResultList();

        for (StagingTransaction st : resultList){
            st.setValidationStage((long)validationStage);
        }
        return resultList.size();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int deleteBatch(int batchSize) {
        final List<StagingTransaction> resultList = entityManager
            .createQuery(FIND_ALL, StagingTransaction.class)
            .setMaxResults(batchSize)
            .getResultList();

        for (StagingTransaction st : resultList){
            entityManager.remove(st);
            // TODO - eclipselink issue here. (still affects EncryptedTransation and EncryptedRawTransaction)
            // When accumulating the changes to be commited to the database, just before applying the delete statements
            // CommitManager.deleteAllObjects
            // eclipselink is trying to sort the StagingTransactions. It uses a TreeMap for this purpose where the key
            // is a org.eclipse.persistence.internal.identitymaps.CacheId.
            // The compareTo implementation in CacheId then tries to cast byte[] to Comparable and fails - and in that
            // case it returns 0 - thus teh TreeMap would only contain one record...
            // as a result - a the maximum number of StagingTransactions one can delete in one go is 1.
            // After switching to a String(varchar) PK the problem is no longer there.
        }
        return resultList.size();
    }
}
