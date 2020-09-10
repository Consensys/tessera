package com.quorum.tessera.p2p;

import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
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
    public void pushBatch() {
        PushBatchRequest pushBatchRequest = new PushBatchRequest(Collections.singletonList("SomeData".getBytes()));
        Response result = recoveryResource.pushBatch(pushBatchRequest);
        assertThat(result.getStatus()).isEqualTo(200);
        verify(resendManager).storeResendBatch(pushBatchRequest);
    }
}
