package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import java.util.NoSuchElementException;
import org.junit.Test;

public class RecoveryClientFactoryTest {

  @Test
  public void newFactory() {

    Config config = mock(Config.class);
    ServerConfig serverConfig = mock(ServerConfig.class);
    when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
    when(config.getP2PServerConfig()).thenReturn(serverConfig);

    RecoveryClientFactory factory = RecoveryClientFactory.newFactory(config);

    assertThat(factory).isExactlyInstanceOf(MockRecoveryClientFactory.class);
  }

  @Test(expected = NoSuchElementException.class)
  public void newFactoryNullCommunicationType() {

    Config config = mock(Config.class);
    ServerConfig serverConfig = mock(ServerConfig.class);
    when(config.getP2PServerConfig()).thenReturn(serverConfig);

    RecoveryClientFactory.newFactory(config);
  }
}
