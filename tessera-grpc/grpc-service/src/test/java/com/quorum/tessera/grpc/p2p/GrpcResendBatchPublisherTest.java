package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.PublishPayloadException;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GrpcResendBatchPublisherTest {

    private GrpcResendBatchPublisher batchPublisher;

    private PayloadEncoder payloadEncoder;

    private P2pClient p2pClient;

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        p2pClient = mock(P2pClient.class);

        batchPublisher = new GrpcResendBatchPublisher(payloadEncoder, p2pClient);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(p2pClient, payloadEncoder);
    }

    @Test
    public void publishSucess() {

        String targetUrl = "http://someplace.com/someresource";
        EncodedPayload payload = mock(EncodedPayload.class);
        List<EncodedPayload> payloads = Arrays.asList(payload);

        when(payloadEncoder.encode(payload)).thenReturn("I love sparrows".getBytes());

        when(p2pClient.pushBatch(anyString(), any())).thenReturn(true);

        batchPublisher.publishBatch(payloads, targetUrl);

        verify(payloadEncoder).encode(payload);
        verify(p2pClient).pushBatch(anyString(), any());

    }

    @Test
    public void publishFail() {

        String targetUrl = "http://someplace.com/someresource";
        EncodedPayload payload = mock(EncodedPayload.class);
        List<EncodedPayload> payloads = Arrays.asList(payload);

        when(payloadEncoder.encode(payload)).thenReturn("I love sparrows".getBytes());

        when(p2pClient.pushBatch(anyString(), any())).thenReturn(false);

        try {
            batchPublisher.publishBatch(payloads, targetUrl);
            failBecauseExceptionWasNotThrown(PublishPayloadException.class);
        } catch (PublishPayloadException ex) {
            verify(payloadEncoder).encode(payload);
            verify(p2pClient).pushBatch(anyString(), any());
        }

    }
    
    @Test
    public void createDefaultInstance() {
        assertThat(new GrpcResendBatchPublisher()).isNotNull();
    }

}
