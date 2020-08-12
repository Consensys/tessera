package com.quorum.tessera.p2p;

import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestNodeInfoPublisherTest {

    private MockClient restClient;

    private PartyInfoParser encoder;

    private NodeInfoPublisher publisher;

    @Before
    public void before() {
        this.restClient = new MockClient();
        this.encoder = mock(PartyInfoParser.class);

        this.publisher = new RestNodeInfoPublisher(restClient, encoder);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(encoder);
    }

    @Test
    public void sendPartyInfo() {
        final NodeInfo sampleNodeInfo = NodeInfo.Builder.create().withUrl("http://sample.com").build();

        final byte[] dataToSend = "sampleEncodedData".getBytes();
        when(encoder.to(any(PartyInfo.class))).thenReturn(dataToSend);

        final byte[] responseData = "Result".getBytes();
        final Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(200);

        final List<Entity> postedEntities = new ArrayList<>();
        final Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();
        doAnswer(
            (invocation) -> {
                postedEntities.add(invocation.getArgument(0));
                return response;
            })
            .when(m)
            .post(any(Entity.class));

        final String targetUrl = "http://somedomain.com";

        final boolean outcome = publisher.publishNodeInfo(targetUrl, sampleNodeInfo);

        assertThat(outcome).isTrue();
        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(dataToSend);

        verify(encoder).to(any(PartyInfo.class));
        verify(response).readEntity(byte[].class);
    }

    @Test
    public void sendPartyInfoReturns201() {
        final NodeInfo sampleNodeInfo = NodeInfo.Builder.create().withUrl("http://sample.com").build();

        final byte[] dataToSend = "sampleEncodedData".getBytes();
        when(encoder.to(any(PartyInfo.class))).thenReturn(dataToSend);

        final byte[] responseData = "Result".getBytes();
        final Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(201);

        final List<Entity> postedEntities = new ArrayList<>();
        final Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();
        doAnswer(
            (invocation) -> {
                postedEntities.add(invocation.getArgument(0));
                return response;
            })
            .when(m)
            .post(any(Entity.class));

        final String targetUrl = "http://somedomain.com";

        final boolean outcome = publisher.publishNodeInfo(targetUrl, sampleNodeInfo);

        assertThat(outcome).isTrue();
        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(dataToSend);

        verify(encoder).to(any(PartyInfo.class));
        verify(response).readEntity(byte[].class);
    }

    @Test
    public void sendPartyInfoReturns400() {
        final NodeInfo sampleNodeInfo = NodeInfo.Builder.create().withUrl("http://sample.com").build();

        when(encoder.to(any(PartyInfo.class))).thenReturn("Encoded".getBytes());

        final Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        final byte[] responseData = "Result".getBytes();
        final Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(400);

        doAnswer((invocation) -> Response.status(400).build())
            .when(m)
            .post(any(Entity.class));

        final String targetUrl = "http://somedomain.com";

        final boolean outcome = publisher.publishNodeInfo(targetUrl, sampleNodeInfo);

        assertThat(outcome).isFalse();

        verify(encoder).to(any(PartyInfo.class));
    }
}
