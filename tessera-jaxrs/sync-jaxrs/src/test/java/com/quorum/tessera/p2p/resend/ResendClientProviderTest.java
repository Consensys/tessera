package com.quorum.tessera.p2p.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import org.junit.Test;

public class ResendClientProviderTest {

  @Test
  public void provider() {

    try (var configFactoryMockedStatic = mockStatic(ConfigFactory.class);
        var clientSSLContextFactoryMockedStatic = mockStatic(ClientSSLContextFactory.class)) {

      ConfigFactory configFactory = mock(ConfigFactory.class);
      Config config = mock(Config.class);
      ServerConfig serverConfig = mock(ServerConfig.class);
      when(config.getP2PServerConfig()).thenReturn(serverConfig);
      when(configFactory.getConfig()).thenReturn(config);

      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);
      clientSSLContextFactoryMockedStatic
          .when(ClientSSLContextFactory::create)
          .thenReturn(mock(ClientSSLContextFactory.class));

      ResendClient resendClient = ResendClientProvider.provider();

      assertThat(resendClient).isNotNull().isExactlyInstanceOf(RestResendClient.class);

      clientSSLContextFactoryMockedStatic.verify(ClientSSLContextFactory::create);
      clientSSLContextFactoryMockedStatic.verifyNoMoreInteractions();

      configFactoryMockedStatic.verify(ConfigFactory::create);
      configFactoryMockedStatic.verifyNoMoreInteractions();
    }
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new ResendClientProvider()).isNotNull();
  }
}
