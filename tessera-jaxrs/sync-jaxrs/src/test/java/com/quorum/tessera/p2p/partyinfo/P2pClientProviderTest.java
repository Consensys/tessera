package com.quorum.tessera.p2p.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.partyinfo.P2pClient;
import org.junit.Test;

public class P2pClientProviderTest {

  @Test
  public void provider() {
    ConfigFactory configFactory = mock(ConfigFactory.class);
    Config config = mock(Config.class);
    when(config.getP2PServerConfig()).thenReturn(mock(ServerConfig.class));
    when(configFactory.getConfig()).thenReturn(config);

    try (var configFactoryMockedStatic = mockStatic(ConfigFactory.class)) {
      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

      P2pClient result = P2pClientProvider.provider();
      assertThat(result).isNotNull().isExactlyInstanceOf(RestP2pClient.class);

      verify(configFactory).getConfig();
      verifyNoMoreInteractions(configFactory);
      configFactoryMockedStatic.verify(ConfigFactory::create);
      configFactoryMockedStatic.verifyNoMoreInteractions();
    }
  }

  @Test
  public void defaultConstrutorForCoverage() {
    assertThat(new P2pClientProvider()).isNotNull();
  }
}
