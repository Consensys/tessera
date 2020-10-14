package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResendClientFactoryTest {

    @Test
    public void newFactory() {

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        ResendClientFactory factory = ResendClientFactory.newFactory(config);

        assertThat(factory).isExactlyInstanceOf(MockResendClientFactory.class);
    }

    @Test(expected = NoSuchElementException.class)
    public void newFactoryNullCommunicationType() {

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        ResendClientFactory.newFactory(config);
    }
}
