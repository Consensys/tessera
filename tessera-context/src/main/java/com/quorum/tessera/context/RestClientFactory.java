package com.quorum.tessera.context;

import com.quorum.tessera.config.ServerConfig;
import java.util.ServiceLoader;
import javax.ws.rs.client.Client;

public interface RestClientFactory {

  Client buildFrom(ServerConfig serverContext);

  static RestClientFactory create() {
    return ServiceLoader.load(RestClientFactory.class).findFirst().get();
  }
}
