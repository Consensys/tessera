package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.P2pClientFactory;

public class WebSocketP2pClientFactory implements P2pClientFactory {
    @Override
    public P2pClient create(Config config) {
        return new WebSocketP2pClient();
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }
}
