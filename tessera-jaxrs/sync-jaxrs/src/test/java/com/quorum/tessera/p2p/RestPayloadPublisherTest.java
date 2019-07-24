package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.partyinfo.PublishPayloadException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestPayloadPublisherTest {

    private RestPayloadPublisher publisher;

    private MockClient mockClient;

    private PayloadEncoder encoder;

    @Before
    public void onSetUp() {
        mockClient = new MockClient();
        encoder = mock(PayloadEncoder.class);
        publisher = new RestPayloadPublisher(mockClient, encoder);
    }

    @Test
    public void publish() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        List<javax.ws.rs.client.Entity> postedEntities = new ArrayList<>();

        doAnswer((invocation) -> {
            postedEntities.add(invocation.getArgument(0));
            return Response.ok().build();
        }).when(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        publisher.publishPayload(encodedPayload, targetUrl);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(payloadData);

        verify(encoder).encode(encodedPayload);
        verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
    }

    @Test
    public void publishReturns201() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        List<javax.ws.rs.client.Entity> postedEntities = new ArrayList<>();

        doAnswer((invocation) -> {
            postedEntities.add(invocation.getArgument(0));
            return Response.created(URI.create("http://location")).build();
        }).when(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        publisher.publishPayload(encodedPayload, targetUrl);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(payloadData);

        verify(encoder).encode(encodedPayload);
        verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
    }

    @Test
    public void publishReturnsError() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        doAnswer((invocation) -> {
            return Response.serverError().build();
        }).when(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        try {
            publisher.publishPayload(encodedPayload, targetUrl);
            failBecauseExceptionWasNotThrown(PublishPayloadException.class);
        } catch (PublishPayloadException ex) {
            verify(encoder).encode(encodedPayload);
            verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
        }

    }
}
