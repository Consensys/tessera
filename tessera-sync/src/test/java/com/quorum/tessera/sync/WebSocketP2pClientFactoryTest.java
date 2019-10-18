package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.partyinfo.P2pClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebSocketP2pClientFactoryTest {

    @Test
    public void create() {
        WebSocketP2pClientFactory factory = new WebSocketP2pClientFactory();
        assertThat(factory.communicationType()).isEqualTo(CommunicationType.WEB_SOCKET);

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.isSsl()).thenReturn(Boolean.FALSE);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);
        P2pClient result = factory.create(config);

        assertThat(result).isNotNull();
    }
}
