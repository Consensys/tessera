package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.shared.Constants;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.Objects;

public class NodeUriHeaderDecorator implements ClientRequestFilter {

  private final ServerConfig serverConfig;

  protected static final String UNKNOWN = "Unknown";

  public NodeUriHeaderDecorator(ServerConfig serverConfig) {
    this.serverConfig = Objects.requireNonNull(serverConfig);
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    final String uri;
    if (serverConfig.isUnixSocket()) {
      uri = serverConfig.getServerAddress();
    } else {
      uri = Objects.toString(serverConfig.getServerUri(), UNKNOWN);
    }
    requestContext.getHeaders().add(Constants.NODE_URI_HEADER, uri);
  }
}
