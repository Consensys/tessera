package com.quorum.tessera.context;

import com.quorum.tessera.config.ServerConfig;
import jakarta.ws.rs.client.Client;
import java.util.ServiceLoader;

public interface RestClientFactory {

  Client buildFrom(ServerConfig serverContext);

  static RestClientFactory create() {
    return ServiceLoader.load(RestClientFactory.class).findFirst().get();
  }
}
