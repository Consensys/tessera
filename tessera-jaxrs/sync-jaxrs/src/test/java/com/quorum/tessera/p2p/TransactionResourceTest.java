package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.*;
import com.quorum.tessera.transaction.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    private TransactionResource transactionResource;

    private TransactionManager transactionManager;
    private BatchResendManager batchResendManager;

    @Before
    public void onSetup() {
        transactionManager = mock(TransactionManager.class);
        batchResendManager = mock(BatchResendManager.class);
        transactionResource = new TransactionResource(transactionManager, batchResendManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void push() {
        byte[] someData = "SomeData".getBytes();
        Response result = transactionResource.push(someData);
        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.hasEntity()).isTrue();
        verify(transactionManager).storePayload(someData);
    }

    @Test
    public void resend() {

        ResendRequest resendRequest = mock(ResendRequest.class);
        ResendResponse resendResponse = new ResendResponse("SUCCESS".getBytes());

        when(transactionManager.resend(resendRequest)).thenReturn(resendResponse);

        Response result = transactionResource.resend(resendRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("SUCCESS".getBytes());
        verify(transactionManager).resend(resendRequest);
    }

    @Test
    public void resendBatch() {
        ResendBatchRequest resendRequest = mock(ResendBatchRequest.class);
        ResendBatchResponse resendResponse = new ResendBatchResponse(1);

        when(batchResendManager.resendBatch(resendRequest)).thenReturn(resendResponse);

        Response result = transactionResource.resendBatch(resendRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(resendResponse);
        verify(batchResendManager).resendBatch(resendRequest);
    }

    @Test
    public void pushBatch() {
        PushBatchRequest pushBatchRequest = new PushBatchRequest(Collections.singletonList("SomeData".getBytes()));
        Response result = transactionResource.pushBatch(pushBatchRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        verify(batchResendManager).storeResendBatch(pushBatchRequest);
    }
}
