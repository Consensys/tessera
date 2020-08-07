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
import static org.mockito.Mockito.doAnswer;

public class RestResendClientTest {

    private MockClient restClient;

    private RestResendClient client;

    @Before
    public void onSetUp() {
        restClient = new MockClient();
        client = new RestResendClient(restClient);
    }

    @Test
    public void makeResendRequest() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        List<Entity> postedEntities = new ArrayList<>();

        doAnswer(
                        (invocation) -> {
                            postedEntities.add(invocation.getArgument(0));
                            return Response.ok().build();
                        })
                .when(m)
                .post(any(Entity.class));

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
    public void makeResendRequestReturns500() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        doAnswer(
                        (invocation) -> {
                            return Response.serverError().build();
                        })
                .when(m)
                .post(any(Entity.class));

        String targetUrl = "http://somedomain.com";
        ResendRequest request = new ResendRequest();

        boolean result = client.makeResendRequest(targetUrl, request);

        assertThat(result).isFalse();
    }
}
