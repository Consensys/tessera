package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class P2pClientFactoryTest {



    @Test(expected = NoSuchElementException.class)
    public void newFactoryNullCommuicationType() {

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        P2pClientFactory.newFactory(config);
    }
}
