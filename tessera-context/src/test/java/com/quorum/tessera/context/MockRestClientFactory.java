package com.quorum.tessera.context;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.ServerConfig;
import javax.ws.rs.client.Client;

public class MockRestClientFactory implements RestClientFactory {

  @Override
  public Client buildFrom(ServerConfig serverContext) {
    return mock(Client.class);
  }
}
