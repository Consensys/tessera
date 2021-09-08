package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.p2p.resend.ResendRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RestRecoveryClientTest {

  private Response.Status expectedResponseStatus;

  public RestRecoveryClientTest(Response.Status expectedResponseStatus) {
    this.expectedResponseStatus = expectedResponseStatus;
  }

  @Test
  public void makeResendRequest() {

    try (var entityMockedStatic = mockStatic(Entity.class)) {

      Entity<ResendRequest> outboundEntity = mock(Entity.class);
      ResendRequest resendRequest = mock(ResendRequest.class);

      entityMockedStatic
          .when(() -> Entity.entity(resendRequest, MediaType.APPLICATION_JSON))
          .thenReturn(outboundEntity);

      String targetUrl = "targetUrl";
      Client client = mock(Client.class);
      WebTarget webTarget = mock(WebTarget.class);
      when(client.target(targetUrl)).thenReturn(webTarget);
      when(webTarget.path("/resend")).thenReturn(webTarget);

      Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
      when(webTarget.request()).thenReturn(invocationBuilder);

      Response response = mock(Response.class);
      when(response.getStatus()).thenReturn(expectedResponseStatus.getStatusCode());

      when(invocationBuilder.post(outboundEntity)).thenReturn(response);

      RestRecoveryClient restRecoveryClient = new RestRecoveryClient(client);

      boolean outcome = restRecoveryClient.makeResendRequest(targetUrl, resendRequest);
      if (expectedResponseStatus == Response.Status.OK) {
        assertThat(outcome).isTrue();
      } else {
        assertThat(outcome).isFalse();
      }

      entityMockedStatic.verify(() -> Entity.entity(resendRequest, MediaType.APPLICATION_JSON));
      entityMockedStatic.verifyNoMoreInteractions();

      verify(client).target(targetUrl);
      verify(webTarget).path("/resend");
      verify(webTarget).request();
      verify(invocationBuilder).post(outboundEntity);

      verifyNoMoreInteractions(outboundEntity, resendRequest, client, webTarget, invocationBuilder);
    }
  }

  @Test
  public void pushBatch() {

    try (var entityMockedStatic = mockStatic(Entity.class)) {

      Entity<PushBatchRequest> outboundEntity = mock(Entity.class);
      PushBatchRequest pushBatchRequest = mock(PushBatchRequest.class);

      entityMockedStatic
          .when(() -> Entity.entity(pushBatchRequest, MediaType.APPLICATION_JSON))
          .thenReturn(outboundEntity);

      String targetUrl = "targetUrl";
      Client client = mock(Client.class);
      WebTarget webTarget = mock(WebTarget.class);
      when(client.target(targetUrl)).thenReturn(webTarget);
      when(webTarget.path("/pushBatch")).thenReturn(webTarget);

      Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
      when(webTarget.request()).thenReturn(invocationBuilder);

      Response response = mock(Response.class);
      when(response.getStatus()).thenReturn(expectedResponseStatus.getStatusCode());

      when(invocationBuilder.post(outboundEntity)).thenReturn(response);

      RestRecoveryClient restRecoveryClient = new RestRecoveryClient(client);

      boolean outcome = restRecoveryClient.pushBatch(targetUrl, pushBatchRequest);
      if (expectedResponseStatus == Response.Status.OK) {
        assertThat(outcome).isTrue();
      } else {
        assertThat(outcome).isFalse();
      }

      entityMockedStatic.verify(() -> Entity.entity(pushBatchRequest, MediaType.APPLICATION_JSON));
      entityMockedStatic.verifyNoMoreInteractions();

      verify(client).target(targetUrl);
      verify(webTarget).path("/pushBatch");
      verify(webTarget).request();
      verify(invocationBuilder).post(outboundEntity);

      verifyNoMoreInteractions(
          outboundEntity, pushBatchRequest, client, webTarget, invocationBuilder);
    }
  }

  @Test
  public void makeBatchResendRequest() {

    try (var entityMockedStatic = mockStatic(Entity.class)) {

      Entity<PushBatchRequest> outboundEntity = mock(Entity.class);
      ResendBatchRequest pushBatchRequest = mock(ResendBatchRequest.class);

      entityMockedStatic
          .when(() -> Entity.entity(pushBatchRequest, MediaType.APPLICATION_JSON))
          .thenReturn(outboundEntity);

      String targetUrl = "targetUrl";
      Client client = mock(Client.class);
      WebTarget webTarget = mock(WebTarget.class);
      when(client.target(targetUrl)).thenReturn(webTarget);
      when(webTarget.path("/resendBatch")).thenReturn(webTarget);

      Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
      when(webTarget.request()).thenReturn(invocationBuilder);

      Response response = mock(Response.class);
      when(response.getStatus()).thenReturn(expectedResponseStatus.getStatusCode());
      ResendBatchResponse resendBatchResponse = mock(ResendBatchResponse.class);
      when(response.readEntity(ResendBatchResponse.class)).thenReturn(resendBatchResponse);

      when(invocationBuilder.post(outboundEntity)).thenReturn(response);

      RestRecoveryClient restRecoveryClient = new RestRecoveryClient(client);

      ResendBatchResponse outcome =
          restRecoveryClient.makeBatchResendRequest(targetUrl, pushBatchRequest);
      if (expectedResponseStatus == Response.Status.OK) {
        verify(response).readEntity(ResendBatchResponse.class);
        assertThat(outcome).isSameAs(resendBatchResponse);
      } else {
        assertThat(outcome).isNull();
      }

      entityMockedStatic.verify(() -> Entity.entity(pushBatchRequest, MediaType.APPLICATION_JSON));
      entityMockedStatic.verifyNoMoreInteractions();

      verify(client).target(targetUrl);
      verify(webTarget).path("/resendBatch");
      verify(webTarget).request();
      verify(invocationBuilder).post(outboundEntity);

      verifyNoMoreInteractions(
          outboundEntity, pushBatchRequest, client, webTarget, invocationBuilder);
    }
  }

  @Parameterized.Parameters(name = "ResponseStatus {0}")
  public static Collection<Response.Status> statuses() {
    return Arrays.asList(Response.Status.values());
  }
}
