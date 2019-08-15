package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.exception.OperationCurrentlySuspended;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.transaction.TransactionManagerFactory.DefaultTransactionManagerFactory;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class TransactionManagerFactoryTest {

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private Enclave enclave;

    private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

    private ResendManager resendManager;

    private PayloadPublisher payloadPublisher;

    @Before
    public void onSetUp() {
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        enclave = mock(Enclave.class);
        encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);
        resendManager = mock(ResendManager.class);
        payloadPublisher = mock(PayloadPublisher.class);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(
                encryptedTransactionDAO, enclave, encryptedRawTransactionDAO, resendManager, payloadPublisher);
    }

    @Test
    public void createAndSendDeleteRequestResendModeOff() {

        SyncState syncState = mock(SyncState.class);
        when(syncState.isResendMode()).thenReturn(Boolean.FALSE);

        TransactionManagerFactory factory = new DefaultTransactionManagerFactory(syncState);

        TransactionManager transactionManager =
                factory.create(
                        encryptedTransactionDAO, enclave, encryptedRawTransactionDAO, resendManager, payloadPublisher);

        assertThat(transactionManager).isNotNull();

        DeleteRequest deleteRequest = mock(DeleteRequest.class);
        when(deleteRequest.getKey()).thenReturn(Base64.getEncoder().encodeToString("FOO".getBytes()));

        transactionManager.delete(deleteRequest);

        verify(encryptedTransactionDAO).delete(any(MessageHash.class));
    }

    @Test
    public void createAndSendDeleteRequestResendModeOn() {

        SyncState syncState = mock(SyncState.class);
        when(syncState.isResendMode()).thenReturn(Boolean.TRUE);

        TransactionManagerFactory factory = new DefaultTransactionManagerFactory(syncState);

        TransactionManager transactionManager =
                factory.create(
                        encryptedTransactionDAO, enclave, encryptedRawTransactionDAO, resendManager, payloadPublisher);

        assertThat(transactionManager).isNotNull();

        DeleteRequest deleteRequest = mock(DeleteRequest.class);
        when(deleteRequest.getKey()).thenReturn(Base64.getEncoder().encodeToString("FOO".getBytes()));

        try {
            transactionManager.delete(deleteRequest);
            failBecauseExceptionWasNotThrown(OperationCurrentlySuspended.class);
        } catch (OperationCurrentlySuspended ex) {
            verifyZeroInteractions(encryptedTransactionDAO);
        }
    }

    // Ensure that InvocationTargetException cause is thrown
    @Test
    public void createAndSendDeleteRequestResendAndInvocationTargetExceptionIsThrown() {

        SyncState syncState = mock(SyncState.class);
        when(syncState.isResendMode()).thenReturn(Boolean.FALSE);

        TransactionManagerFactory factory = new DefaultTransactionManagerFactory(syncState);

        TransactionManager transactionManager =
                factory.create(
                        encryptedTransactionDAO, enclave, encryptedRawTransactionDAO, resendManager, payloadPublisher);

        assertThat(transactionManager).isNotNull();

        UnsupportedOperationException cause = new UnsupportedOperationException("Ouch");

        DeleteRequest deleteRequest = mock(DeleteRequest.class);
        when(deleteRequest.getKey()).thenThrow(cause);

        try {
            transactionManager.delete(deleteRequest);
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException ex) {
            verifyZeroInteractions(encryptedTransactionDAO);
        }
    }

    @Test
    public void createDefaultInstance() {
        assertThat(TransactionManagerFactory.newFactory())
                .isNotNull()
                .isExactlyInstanceOf(DefaultTransactionManagerFactory.class);
    }
}
