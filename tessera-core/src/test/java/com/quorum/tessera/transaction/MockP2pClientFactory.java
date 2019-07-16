
package com.quorum.tessera.transaction;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.P2pClientFactory;
import static org.mockito.Mockito.mock;

public class MockP2pClientFactory implements P2pClientFactory {

    @Override
    public P2pClient create(Config config) {
        return mock(P2pClient.class);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

}
