package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.shared.Constants;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.Objects;

public class NodeUriHeaderDecorator implements ClientRequestFilter {

  private final ServerConfig serverConfig;

  public NodeUriHeaderDecorator(ServerConfig serverConfig) {
    this.serverConfig = Objects.requireNonNull(serverConfig);
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    requestContext
        .getHeaders()
        .add(Constants.NODE_URI_HEADER, Objects.toString(serverConfig.getServerUri(), "Unknown"));
  }
}
