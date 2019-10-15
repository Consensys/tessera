
package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PartyInfoPoller;
import com.quorum.tessera.partyinfo.PartyInfoService;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;


public class WebsocketPartyInfoPollerFactoryTest {
    
    @Test
    public void createFactoryAndThenPoller() {
        WebsocketPartyInfoPollerFactory websocketPartyInfoPollerFactory = new WebsocketPartyInfoPollerFactory();
        assertThat(websocketPartyInfoPollerFactory.communicationType()).isEqualTo(CommunicationType.WEB_SOCKET);
        
        PartyInfoService partyInfoService = mock(PartyInfoService.class);
        Config config = mock(Config.class);
        PartyInfoPoller partyInfoPoller = websocketPartyInfoPollerFactory.create(partyInfoService, config);
        
        assertThat(partyInfoPoller).isExactlyInstanceOf(WebsocketPartyInfoPoller.class);
    }
    
    
}
