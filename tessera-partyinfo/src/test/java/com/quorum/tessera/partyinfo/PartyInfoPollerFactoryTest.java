package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import java.util.NoSuchElementException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartyInfoPollerFactoryTest {

    @Test
    public void newFactoryAndCreatePoller() {
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.WEB_SOCKET);
        Config config = mock(Config.class);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        PartyInfoPollerFactory partyInfoPollerFactory = PartyInfoPollerFactory.newFactory(config);

        assertThat(partyInfoPollerFactory).isExactlyInstanceOf(MockPartyInfoPollerFactory.class);

        PartyInfoService partyInfoService = mock(PartyInfoService.class);

        PartyInfoPoller poller = partyInfoPollerFactory.create(partyInfoService, config);
        assertThat(poller).isExactlyInstanceOf(P2pClientPartyInfoPoller.class);
        
    }
    
    @Test(expected = NoSuchElementException.class)
    public void newFactoryNoFactoryForCommunicationType() {
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        Config config = mock(Config.class);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        PartyInfoPollerFactory.newFactory(config);

        
    }

}
