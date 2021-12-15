package com.quorum.tessera.p2p.resend;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

class RestResendClient implements ResendClient {

  private final Client client;

  RestResendClient(final Client client) {
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
