package com.quorum.tessera.p2p;

import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.jaxrs.mock.MockWebTarget;
import com.quorum.tessera.partyinfo.node.Party;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
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

        MockWebTarget webTarget = restClient.getWebTarget();
        Invocation.Builder m = webTarget.getMockInvocationBuilder();

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
        MockWebTarget webTarget = restClient.getWebTarget();

        Invocation.Builder m = webTarget.getMockInvocationBuilder();
        byte[] responseData = "Result".getBytes();

        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(400);

        when(m.post(any(Entity.class))).thenReturn(response);

        String targetUrl = "http://somedomain.com";
        byte[] data = "Some Data".getBytes();

        boolean outcome = client.sendPartyInfo(targetUrl, data);

        assertThat(outcome).isFalse();
    }

    @Test
    public void getParties() {
        MockWebTarget webTarget = restClient.getWebTarget();
        Invocation.Builder m = webTarget.getMockInvocationBuilder();

        JsonObject responseData = Json.createObjectBuilder()
            .add("peers",Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("url","http://hughfitzcairn.com")))
            .build();


        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(JsonObject.class)).thenReturn(responseData);

        when(m.get()).thenReturn(response);

        URI uri = URI.create("http://hughfitzcairn.com/");
        Stream<Party> result = client.getParties(uri);

        assertThat(result).containsExactly(new Party(uri.toString()));


    }

    @Test
    public void getPartiesError() {
        MockWebTarget webTarget = restClient.getWebTarget();
        Invocation.Builder m = webTarget.getMockInvocationBuilder();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(500);

        Response.StatusType statusType = mock(Response.StatusType.class);
        when(statusType.getReasonPhrase()).thenReturn("OUCH");
        when(response.getStatusInfo()).thenReturn(statusType);

        when(m.get()).thenReturn(response);

        URI uri = URI.create("http://hughfitzcairn.com/");

        try {
            client.getParties(uri);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        } catch (RuntimeException ex) {
            assertThat(ex)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageContaining("OUCH");
        }



    }
}
