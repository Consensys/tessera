package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.partyinfo.P2pClient;
import java.util.Objects;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RestP2pClient implements P2pClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestP2pClient.class);

  private final Client client;

  RestP2pClient(final Client client) {
    this.client = Objects.requireNonNull(client);
  }

  @Override
  public boolean sendPartyInfo(final String targetUrl, final byte[] data) {
    LOGGER.debug("Sending PartyInfo to peer {}", targetUrl);

    try (Response response =
        client
            .target(targetUrl)
            .path("/partyinfo")
            .request()
            .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

      final int returnStatusCode = response.getStatus();
      if (Status.OK.getStatusCode() != returnStatusCode
          && Status.CREATED.getStatusCode() != returnStatusCode) {
        LOGGER.warn(
            "PartyInfo returned status code for peer {} was {}", targetUrl, returnStatusCode);
        return false;
      }

      LOGGER.debug("Successful PartyInfo call to {}", targetUrl);
      return Objects.nonNull(response.readEntity(byte[].class));
    }
  }
}
