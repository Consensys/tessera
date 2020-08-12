package com.quorum.tessera.p2p;

import com.quorum.tessera.jaxrs.mock.MockClient;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void sendPartyInfo() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();
        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(200);

        List<Entity> postedEntities = new ArrayList<>();
        doAnswer(
                        (invocation) -> {
                            postedEntities.add(invocation.getArgument(0));
                            return response;
                        })
                .when(m)
                .post(any(Entity.class));

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
    public void sendPartyInfoReturns400() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();

        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(400);

        doAnswer(
                        (invocation) -> {
                            return Response.status(400).build();
                        })
                .when(m)
                .post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        boolean outcome = client.sendPartyInfo(targetUrl, data);

        assertThat(outcome).isFalse();
    }
}
