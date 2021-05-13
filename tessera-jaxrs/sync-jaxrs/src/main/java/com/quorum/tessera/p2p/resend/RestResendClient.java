package com.quorum.tessera.p2p.resend;

import java.util.Objects;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestResendClient implements ResendClient {

  private final Client client;

  public RestResendClient(final Client client) {
    this.client = Objects.requireNonNull(client);
  }

  @Override
  public boolean makeResendRequest(final String targetUrl, final ResendRequest request) {
    final Entity<ResendRequest> outboundEntity = Entity.entity(request, MediaType.APPLICATION_JSON);

    try (Response response =
        client.target(targetUrl).path("/resend").request().post(outboundEntity)) {
      return Response.Status.OK.getStatusCode() == response.getStatus();
    }
  }
}
