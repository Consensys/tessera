package com.quorum.tessera.p2p;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    private TransactionResource transactionResource;

    private TransactionManager transactionManager;

    private PayloadEncoder payloadEncoder;

    @Before
    public void onSetup() {
        this.transactionManager = mock(TransactionManager.class);
        this.payloadEncoder = mock(PayloadEncoder.class);
        transactionResource = new TransactionResource(transactionManager,payloadEncoder);

        this.transactionResource = new TransactionResource(transactionManager, payloadEncoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionManager,payloadEncoder);
    }

    @Test
    public void push() {
        final byte[] someData = "SomeData".getBytes();
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(someData)).thenReturn(payload);

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("SomeMoreData".getBytes());

        when(transactionManager.storePayload(payload)).thenReturn(transactionHash);

        final Response result = transactionResource.push(someData);

        assertThat(result.getStatus()).isEqualTo(201);
        assertThat(result.hasEntity()).isTrue();
        verify(transactionManager).storePayload(payload);
        verify(payloadEncoder).decode(someData);
    }

    @Test
    public void resend() {
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setType("ALL");
        resendRequest.setPublicKey(Base64.getEncoder().encodeToString("JUNIT".getBytes()));

        EncodedPayload payload = mock(EncodedPayload.class);
        com.quorum.tessera.transaction.ResendResponse resendResponse = mock(com.quorum.tessera.transaction.ResendResponse.class);
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

}
