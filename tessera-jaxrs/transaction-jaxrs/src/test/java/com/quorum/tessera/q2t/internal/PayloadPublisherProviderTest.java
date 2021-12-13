package com.quorum.tessera.q2t.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.Test;

public class PayloadPublisherProviderTest {

  @Test
  public void provider() {

    ConfigFactory configFactory = mock(ConfigFactory.class);
    Config config = mock(Config.class);
    ServerConfig serverConfig = mock(ServerConfig.class);
    when(config.getP2PServerConfig()).thenReturn(serverConfig);
    when(configFactory.getConfig()).thenReturn(config);

    try (var configFactoryMockedStatic = mockStatic(ConfigFactory.class);
        var discoveryMockedStatic = mockStatic(Discovery.class)) {

      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);
      discoveryMockedStatic.when(Discovery::create).thenReturn(mock(Discovery.class));

      PayloadPublisher payloadPublisher = PayloadPublisherProvider.provider();
      assertThat(payloadPublisher).isNotNull();

      configFactoryMockedStatic.verify(ConfigFactory::create);
      configFactoryMockedStatic.verifyNoMoreInteractions();

      discoveryMockedStatic.verify(Discovery::create);
      discoveryMockedStatic.verifyNoMoreInteractions();
    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new PayloadPublisherProvider()).isNotNull();
  }
}
