package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.sync.ResendClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestResendClientFactoryTest {

    @Test
    public void create() {
        RestResendClientFactory factory = new RestResendClientFactory();
        assertThat(factory.communicationType()).isEqualTo(CommunicationType.REST);

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.isSsl()).thenReturn(Boolean.FALSE);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);
        ResendClient result = factory.create(config);

        assertThat(result).isNotNull();
    }
}
