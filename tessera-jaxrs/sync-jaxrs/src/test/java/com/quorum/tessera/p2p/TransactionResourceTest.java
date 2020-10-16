package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.p2p.recovery.ResendBatchRequest;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.p2p.resend.ResendRequestType;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    private TransactionResource transactionResource;

    private TransactionManager transactionManager;

    private BatchResendManager batchResendManager;

    private PayloadEncoder payloadEncoder;

    @Before
    public void onSetup() {
        transactionManager = mock(TransactionManager.class);
        batchResendManager = mock(BatchResendManager.class);
        this.payloadEncoder = mock(PayloadEncoder.class);
        transactionResource = new TransactionResource(transactionManager, batchResendManager, payloadEncoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionManager, batchResendManager, payloadEncoder);
    }

    @Test
    public void push() {
        final byte[] someData = "SomeData".getBytes();
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(someData)).thenReturn(payload);

        final Response result = transactionResource.push(someData);

        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.hasEntity()).isTrue();
        verify(transactionManager).storePayload(payload);
        verify(payloadEncoder).decode(someData);
    }

    @Test
    public void resend() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType(ResendRequestType.ALL);
        resendRequest.setPublicKey(Base64.getEncoder().encodeToString("JUNIT".getBytes()));

        EncodedPayload payload = mock(EncodedPayload.class);
        com.quorum.tessera.transaction.ResendResponse resendResponse =
                mock(com.quorum.tessera.transaction.ResendResponse.class);
        when(resendResponse.getPayload()).thenReturn(payload);

        when(transactionManager.resend(any(com.quorum.tessera.transaction.ResendRequest.class)))
                .thenReturn(resendResponse);

        when(payloadEncoder.encode(payload)).thenReturn("SUCCESS".getBytes());

        Response result = transactionResource.resend(resendRequest);

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("SUCCESS".getBytes());
        verify(transactionManager).resend(any(com.quorum.tessera.transaction.ResendRequest.class));

        verify(payloadEncoder).encode(payload);
    }

    @Test
    public void resendBatch() {

        ResendBatchRequest incoming = new ResendBatchRequest();
        incoming.setPublicKey("someKey");
        incoming.setBatchSize(1);

        ResendBatchResponse resendResponse = ResendBatchResponse.from(1);
        when(batchResendManager.resendBatch(any())).thenReturn(resendResponse);

        Response result = transactionResource.resendBatch(incoming);
        assertThat(result.getStatus()).isEqualTo(200);
        com.quorum.tessera.p2p.recovery.ResendBatchResponse convertedResponse =
                (com.quorum.tessera.p2p.recovery.ResendBatchResponse) result.getEntity();

        assertThat(convertedResponse.getTotal()).isEqualTo(1);

        ArgumentCaptor<com.quorum.tessera.recovery.resend.ResendBatchRequest> captor =
                ArgumentCaptor.forClass(com.quorum.tessera.recovery.resend.ResendBatchRequest.class);

        verify(batchResendManager).resendBatch(captor.capture());

        com.quorum.tessera.recovery.resend.ResendBatchRequest convertedRequest = captor.getValue();

        assertThat(convertedRequest.getPublicKey()).isEqualTo("someKey");
        assertThat(convertedRequest.getBatchSize()).isEqualTo(1);
    }
}
