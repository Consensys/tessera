package com.quorum.tessera.p2p.partyinfo;

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
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RestP2pClientTest {

  private Response.Status expectedResponseStatus;

  public RestP2pClientTest(Response.Status expectedResponseStatus) {
    this.expectedResponseStatus = expectedResponseStatus;
  }

  @Test
  public void sendPartyInfo() {
    try (var entityMockedStatic = mockStatic(Entity.class)) {

      Entity<ResendRequest> outboundEntity = mock(Entity.class);
      byte[] partyinfoData = "SomeEncodedPartyInfoData".getBytes();

      entityMockedStatic
          .when(() -> Entity.entity(partyinfoData, MediaType.APPLICATION_OCTET_STREAM_TYPE))
          .thenReturn(outboundEntity);

      String targetUrl = "targetUrl";
      Client client = mock(Client.class);
      WebTarget webTarget = mock(WebTarget.class);
      when(client.target(targetUrl)).thenReturn(webTarget);
      when(webTarget.path("/partyinfo")).thenReturn(webTarget);

      Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
      when(webTarget.request()).thenReturn(invocationBuilder);

      Response response = mock(Response.class);
      when(response.getStatus()).thenReturn(expectedResponseStatus.getStatusCode());
      when(response.readEntity(byte[].class)).thenReturn("Success".getBytes());

      when(invocationBuilder.post(outboundEntity)).thenReturn(response);

      RestP2pClient restP2pClient = new RestP2pClient(client);

      boolean outcome = restP2pClient.sendPartyInfo(targetUrl, partyinfoData);
      if (Set.of(Response.Status.OK, Response.Status.CREATED).contains(expectedResponseStatus)) {
        assertThat(outcome).isTrue();
      } else {
        assertThat(outcome).isFalse();
      }

      entityMockedStatic.verify(
          () -> Entity.entity(partyinfoData, MediaType.APPLICATION_OCTET_STREAM_TYPE));
      entityMockedStatic.verifyNoMoreInteractions();

      verify(client).target(targetUrl);
      verify(webTarget).path("/partyinfo");
      verify(webTarget).request();
      verify(invocationBuilder).post(outboundEntity);

      verifyNoMoreInteractions(outboundEntity, client, webTarget, invocationBuilder);
    }
  }

  @Parameterized.Parameters(name = "ResponseStatus {0}")
  public static Collection<Response.Status> statuses() {
    return Arrays.asList(Response.Status.values());
  }
}
