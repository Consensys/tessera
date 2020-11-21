package com.quorum.tessera.migration;

import com.quorum.tessera.data.EncryptedRawTransaction;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class RawTransactionMigratorTest {

    private EncryptedRawTransactionDAO primaryDao;

    private EncryptedRawTransactionDAO secondaryDao;

    private RawTransactionMigrator migrator;

    @Before
    public void init() {
        this.primaryDao = mock(EncryptedRawTransactionDAO.class);
        this.secondaryDao = mock(EncryptedRawTransactionDAO.class);

        this.migrator = new RawTransactionMigrator(primaryDao, secondaryDao);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(primaryDao, secondaryDao);
    }

    @Test
    public void singleBatchOnlyCallsOnce() {
        final MessageHash testTxHash = new MessageHash("testHash".getBytes());
        final EncryptedRawTransaction testTx = new EncryptedRawTransaction();
        testTx.setHash(testTxHash);

        when(secondaryDao.transactionCount()).thenReturn(1L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(testTx));
        when(primaryDao.retrieveByHash(testTxHash)).thenReturn(Optional.empty());

        migrator.migrate();

        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(primaryDao).retrieveByHash(testTxHash);
        verify(primaryDao).save(testTx);
    }

    @Test
    public void multipleBatchesForLargeCounts() {
        final MessageHash testTxHash = new MessageHash("testHash".getBytes());
        final EncryptedRawTransaction testTx = new EncryptedRawTransaction();
        testTx.setHash(testTxHash);

        when(secondaryDao.transactionCount()).thenReturn(201L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(testTx));
        when(secondaryDao.retrieveTransactions(100, 100)).thenReturn(List.of(testTx));
        when(secondaryDao.retrieveTransactions(200, 100)).thenReturn(List.of(testTx));
        when(primaryDao.retrieveByHash(testTxHash)).thenReturn(Optional.empty());

        migrator.migrate();

        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(secondaryDao).retrieveTransactions(100, 100);
        verify(secondaryDao).retrieveTransactions(200, 100);
        verify(primaryDao, times(3)).retrieveByHash(testTxHash);
        verify(primaryDao, times(3)).save(testTx);
    }

    @Test
    public void dontCopyIfExistsInPrimary() {
        final MessageHash testTxHash = new MessageHash("testHash".getBytes());
        final EncryptedRawTransaction testTx = new EncryptedRawTransaction();
        testTx.setHash(testTxHash);

        when(secondaryDao.transactionCount()).thenReturn(1L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(testTx));
        when(primaryDao.retrieveByHash(testTxHash)).thenReturn(Optional.of(testTx));

        migrator.migrate();

        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(primaryDao).retrieveByHash(testTxHash);
    }

    @Test
    public void jdbcErrorStopsProcessing() {
        final MessageHash testTxHash = new MessageHash("testHash".getBytes());
        final EncryptedRawTransaction testTx = new EncryptedRawTransaction();
        testTx.setHash(testTxHash);

        final MessageHash testTxHash2 = new MessageHash("testHash2".getBytes());
        final EncryptedRawTransaction testTx2 = new EncryptedRawTransaction();
        testTx2.setHash(testTxHash2);

        when(secondaryDao.transactionCount()).thenReturn(2L);
        when(secondaryDao.retrieveTransactions(0, 100)).thenReturn(List.of(testTx, testTx2));
        when(primaryDao.retrieveByHash(testTxHash)).thenThrow(PersistenceException.class);

        final Throwable throwable = catchThrowable(migrator::migrate);

        assertThat(throwable).isInstanceOf(PersistenceException.class);

        verify(secondaryDao).transactionCount();
        verify(secondaryDao).retrieveTransactions(0, 100);
        verify(primaryDao).retrieveByHash(testTxHash);
    }
}
