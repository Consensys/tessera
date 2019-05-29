package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendResponse;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    private TransactionResource transactionResource;

    private TransactionManager transactionManager;

    @Before
    public void onSetup() {

        transactionManager = mock(TransactionManager.class);
        transactionResource = new TransactionResource(transactionManager);

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
}
