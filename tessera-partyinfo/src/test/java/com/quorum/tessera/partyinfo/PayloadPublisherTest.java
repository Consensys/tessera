package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class PayloadPublisherTest {

    private PayloadPublisher payloadPublisher;

    private PayloadEncoder payloadEncoder;

    private P2pClient p2pClient;

    @Before
    public void onSetUp() {
        this.payloadEncoder = mock(PayloadEncoder.class);
        this.p2pClient = mock(P2pClient.class);

        this.payloadPublisher = new PayloadPublisherImpl(payloadEncoder, p2pClient);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, p2pClient);
    }

    @Test
    public void publish() {

        byte[] payloadData = "HELLOW".getBytes();
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

        String targetUrl = "targetUrl";

        byte[] pushResponse = "Push Response".getBytes();

        when(p2pClient.push(targetUrl, payloadData)).thenReturn(pushResponse);

        payloadPublisher.publishPayload(encodedPayload, targetUrl);

        verify(payloadEncoder).encode(encodedPayload);
        verify(p2pClient).push(targetUrl, payloadData);
    }

    @Test
    public void publishThrowsPublishPayloadExceptionWhenNullResponseOnPush() {

        byte[] payloadData = "HELLOW".getBytes();
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

        String targetUrl = "targetUrl";

        try {
            payloadPublisher.publishPayload(encodedPayload, targetUrl);
            failBecauseExceptionWasNotThrown(PublishPayloadException.class);
        } catch (PublishPayloadException ex) {
            verify(payloadEncoder).encode(encodedPayload);
            verify(p2pClient).push(targetUrl, payloadData);
        }
    }
}
