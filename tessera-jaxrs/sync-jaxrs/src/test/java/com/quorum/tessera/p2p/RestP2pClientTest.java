package com.quorum.tessera.p2p;

import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.partyinfo.ResendRequest;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestP2pClientTest {

    private MockClient restClient;

    private RestP2pClient client;

    @Before
    public void onSetUp() {
        restClient = new MockClient();
        client = new RestP2pClient(restClient);
    }

    @Test
    public void makeResendRequest() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        List<Entity> postedEntities = new ArrayList<>();

        doAnswer((invocation) -> {
            postedEntities.add(invocation.getArgument(0));
            return Response.ok().build();
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        ResendRequest request = new ResendRequest();

        boolean result = client.makeResendRequest(targetUrl, request);

        assertThat(postedEntities).hasSize(1);
        assertThat(result).isTrue();

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE);
        assertThat(entity.getEntity()).isSameAs(request);

    }

    @Test
    public void sendPartyInfo() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();
        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(200);

        List<Entity> postedEntities = new ArrayList<>();
        doAnswer((invocation) -> {
            postedEntities.add(invocation.getArgument(0));
            return response;
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        boolean outcome = client.sendPartyInfo(targetUrl, data);

        assertThat(outcome).isTrue();
        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(data);

        verify(response).readEntity(byte[].class);

    }

    @Test
    public void push() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();
        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(200);

        List<Entity> postedEntities = new ArrayList<>();
        doAnswer((invocation) -> {
            postedEntities.add(invocation.getArgument(0));
            return response;
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        byte[] outcome = client.push(targetUrl, data);

        assertThat(outcome).isSameAs(responseData);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(data);

        verify(response).readEntity(byte[].class);

    }

    @Test
    public void pushReturns201() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();

        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(201);

        List<Entity> postedEntities = new ArrayList<>();
        doAnswer((invocation) -> {
            postedEntities.add(invocation.getArgument(0));
            return response;
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        byte[] outcome = client.push(targetUrl, data);

        assertThat(outcome).isSameAs(responseData);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(data);

        verify(response).readEntity(byte[].class);

    }

    @Test
    public void sendPartyInfoReturns201() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();

        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(201);

        List<Entity> postedEntities = new ArrayList<>();
        doAnswer((invocation) -> {
            postedEntities.add(invocation.getArgument(0));
            return response;
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        boolean outcome = client.sendPartyInfo(targetUrl, data);

        assertThat(outcome).isTrue();
        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(data);

        verify(response).readEntity(byte[].class);

    }

    @Test
    public void pushReturns400() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        doAnswer((invocation) -> {
            return Response.status(400).build();
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        byte[] outcome = client.push(targetUrl, data);

        assertThat(outcome).isNull();

    }

    @Test
    public void sendPartyInfoReturns400() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();

        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(400);

        doAnswer((invocation) -> {
            return Response.status(400).build();
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        boolean outcome = client.sendPartyInfo(targetUrl, data);

        assertThat(outcome).isFalse();

    }

    @Test
    public void makeResendRequestReturns500() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        doAnswer((invocation) -> {
            return Response.serverError().build();
        }).when(m).post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        ResendRequest request = new ResendRequest();

        boolean result = client.makeResendRequest(targetUrl, request);

        assertThat(result).isFalse();

    }

}
