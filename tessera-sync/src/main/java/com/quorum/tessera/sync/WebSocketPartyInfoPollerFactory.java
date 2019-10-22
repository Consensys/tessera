package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PartyInfoPoller;
import com.quorum.tessera.partyinfo.PartyInfoPollerFactory;
import com.quorum.tessera.partyinfo.PartyInfoService;

public class WebSocketPartyInfoPollerFactory implements PartyInfoPollerFactory {

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }

    @Override
    public PartyInfoPoller create(PartyInfoService partyInfoService, Config config) {
        return new WebSocketPartyInfoPoller(partyInfoService);
    }
}
