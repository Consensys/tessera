package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PushBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;
import com.quorum.tessera.recover.resend.BatchResendManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecoveryResourceTest {

    private RecoveryResource recoveryResource;

    private BatchResendManager resendManager;

    @Before
    public void onSetup() {
        resendManager = mock(BatchResendManager.class);
        recoveryResource = new RecoveryResource(resendManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(resendManager);
    }

    @Test
    public void resendBatch() {
        ResendBatchRequest resendRequest = mock(ResendBatchRequest.class);
        ResendBatchResponse resendResponse = new ResendBatchResponse(1);

        when(resendManager.resendBatch(resendRequest)).thenReturn(resendResponse);

        Response result = recoveryResource.resendBatch(resendRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(resendResponse);
        verify(resendManager).resendBatch(resendRequest);
    }

    @Test
    public void pushBatch() {
        PushBatchRequest pushBatchRequest = new PushBatchRequest(Collections.singletonList("SomeData".getBytes()));
        Response result = recoveryResource.pushBatch(pushBatchRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        verify(resendManager).storeResendBatch(pushBatchRequest);
    }
}
