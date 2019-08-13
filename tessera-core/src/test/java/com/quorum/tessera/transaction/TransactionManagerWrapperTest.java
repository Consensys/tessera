package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.exception.OperationCurrentlySuspended;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class TransactionManagerWrapperTest {
    private TransactionManager transactionManager;

    private TransactionManagerWrapper wrapper;

    @Before
    public void setUp() {
        transactionManager = mock(TransactionManager.class);

        wrapper = new TransactionManagerWrapper(transactionManager);
        wrapper.setResendMode(true);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(transactionManager);
    }

    @Test(expected = OperationCurrentlySuspended.class)
    public void sendSuspended() {
        wrapper.send(mock(SendRequest.class));
    }

    @Test
    public void send() {
        wrapper.setResendMode(false);
        SendRequest mock = mock(SendRequest.class);
        wrapper.send(mock);
        verify(transactionManager).send(mock);
    }

    @Test(expected = OperationCurrentlySuspended.class)
    public void sendSignedTransactionSuspended() {
        wrapper.sendSignedTransaction(mock(SendSignedRequest.class));
    }

    @Test
    public void sendSignedTransaction() {
        wrapper.setResendMode(false);
        SendSignedRequest mock = mock(SendSignedRequest.class);
        wrapper.sendSignedTransaction(mock);
        verify(transactionManager).sendSignedTransaction(mock);
    }

    @Test(expected = OperationCurrentlySuspended.class)
    public void deleteSuspended() {
        wrapper.delete(mock(DeleteRequest.class));
    }

    @Test
    public void delete() {
        wrapper.setResendMode(false);
        DeleteRequest mock = mock(DeleteRequest.class);
        wrapper.delete(mock);
        verify(transactionManager).delete(mock);
    }

    @Test(expected = OperationCurrentlySuspended.class)
    public void resendSuspended() {
        wrapper.resend(mock(ResendRequest.class));
    }

    @Test
    public void resend() {
        wrapper.setResendMode(false);
        ResendRequest mock = mock(ResendRequest.class);
        wrapper.resend(mock);
        verify(transactionManager).resend(mock);
    }

    @Test(expected = OperationCurrentlySuspended.class)
    public void storePayloadSuspended() {
        wrapper.storePayload("payload".getBytes());
    }

    @Test
    public void storePayload() {
        wrapper.setResendMode(false);
        byte[] bytes = "payload".getBytes();
        wrapper.storePayload(bytes);
        verify(transactionManager).storePayload(bytes);
    }

    @Test
    public void storePayloadBypassSuspended() {
        byte[] bytes = "payload".getBytes();
        wrapper.storePayloadBypass(bytes);
        verify(transactionManager).storePayload(bytes);
    }

    @Test
    public void storePayloadBypass() {
        wrapper.setResendMode(false);
        byte[] bytes = "payload".getBytes();
        wrapper.storePayloadBypass(bytes);
        verify(transactionManager).storePayload(bytes);
    }

    @Test(expected = OperationCurrentlySuspended.class)
    public void receiveSuspended() {
        wrapper.receive(mock(ReceiveRequest.class));
    }

    @Test
    public void receive() {
        wrapper.setResendMode(false);
        ReceiveRequest mock = mock(ReceiveRequest.class);
        wrapper.receive(mock);
        verify(transactionManager).receive(mock);
    }

    @Test(expected = OperationCurrentlySuspended.class)
    public void storeSuspended() {
        wrapper.store(mock(StoreRawRequest.class));
    }

    @Test
    public void store() {
        wrapper.setResendMode(false);
        StoreRawRequest mock = mock(StoreRawRequest.class);
        wrapper.store(mock);
        verify(wrapper.getTransactionManager()).store(mock);
    }
}
