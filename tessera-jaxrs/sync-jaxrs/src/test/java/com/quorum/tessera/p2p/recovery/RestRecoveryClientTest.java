package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.p2p.resend.ResendRequest;
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

public class RestRecoveryClientTest {

    private MockClient restClient;

    private RestRecoveryClient client;

    @Before
    public void onSetUp() {
        restClient = new MockClient();
        client = new RestRecoveryClient(restClient);
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

    @Test
    public void pushBatch() {

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

        PushBatchRequest pushBatchRequest = mock(PushBatchRequest.class);

        boolean result = client.pushBatch(targetUrl, pushBatchRequest);

        assertThat(result).isTrue();

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE);
        assertThat(entity.getEntity()).isSameAs(pushBatchRequest);
    }

    @Test
    public void pushBatchReturns500() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        byte[] responseData = "Result".getBytes();
        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(500);

        when(m.post(any(Entity.class))).thenReturn(response);

        String targetUrl = "http://somedomain.com";

        PushBatchRequest pushBatchRequest = mock(PushBatchRequest.class);

        boolean result = client.pushBatch(targetUrl, pushBatchRequest);

        assertThat(result).isFalse();
    }

    @Test
    public void makeBatchResendRequest() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        ResendBatchResponse responseData = mock(ResendBatchResponse.class);
        Response response = mock(Response.class);
        when(response.readEntity(ResendBatchResponse.class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(200);

        when(m.post(any(Entity.class))).thenReturn(response);

        String targetUrl = "http://somedomain.com";

        ResendBatchRequest resendBatchRequest = mock(ResendBatchRequest.class);
        ResendBatchResponse resendBatchResponse = client.makeBatchResendRequest(targetUrl, resendBatchRequest);

        assertThat(resendBatchResponse).isNotNull().isSameAs(responseData);
    }

    @Test
    public void makeBatchResendRequestServerError() {

        Invocation.Builder m = restClient.getWebTarget().getMockInvocationBuilder();

        ResendBatchResponse responseData = mock(ResendBatchResponse.class);
        Response response = mock(Response.class);
        when(response.readEntity(ResendBatchResponse.class)).thenReturn(responseData);
        when(response.getStatus()).thenReturn(500);

        when(m.post(any(Entity.class))).thenReturn(response);

        String targetUrl = "http://somedomain.com";

        ResendBatchRequest resendBatchRequest = mock(ResendBatchRequest.class);
        ResendBatchResponse resendBatchResponse = client.makeBatchResendRequest(targetUrl, resendBatchRequest);

        assertThat(resendBatchResponse).isNull();
    }
}
