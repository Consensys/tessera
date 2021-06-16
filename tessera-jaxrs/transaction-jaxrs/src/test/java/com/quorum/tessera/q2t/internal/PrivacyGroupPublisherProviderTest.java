package com.quorum.tessera.q2t.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import org.junit.Test;

public class PrivacyGroupPublisherProviderTest {

  @Test
  public void provider() throws Exception {

    Config config = mock(Config.class);
    when(config.getP2PServerConfig()).thenReturn(mock(ServerConfig.class));

    ConfigFactory configFactory = mock(ConfigFactory.class);
    when(configFactory.getConfig()).thenReturn(config);

    PrivacyGroupPublisher result;
    try (var discoveryMockedStatic = mockStatic(Discovery.class);
        var configFactoryMockedStatic = mockStatic(ConfigFactory.class); ) {

      discoveryMockedStatic.when(Discovery::create).thenReturn(mock(Discovery.class));
      configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);

      result = PrivacyGroupPublisherProvider.provider();

      discoveryMockedStatic.verify(Discovery::create);
      configFactoryMockedStatic.verify(ConfigFactory::create);

      discoveryMockedStatic.verifyNoMoreInteractions();
      configFactoryMockedStatic.verifyNoMoreInteractions();
    }

    assertThat(result).isNotNull();
  }

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new PrivacyGroupPublisherProvider()).isNotNull();
  }
}
