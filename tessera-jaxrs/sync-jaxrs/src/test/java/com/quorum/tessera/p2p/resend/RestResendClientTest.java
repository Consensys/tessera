package com.quorum.tessera.p2p.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
public class RestResendClientTest {

  private Response.Status expectedResponseStatus;

  public RestResendClientTest(Response.Status expectedResponseStatus) {
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

      RestResendClient restResendClient = new RestResendClient(client);

      boolean outcome = restResendClient.makeResendRequest(targetUrl, resendRequest);
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

  @Parameterized.Parameters(name = "ResponseStatus {0}")
  public static Collection<Response.Status> statuses() {
    return Arrays.asList(Response.Status.values());
  }
}
