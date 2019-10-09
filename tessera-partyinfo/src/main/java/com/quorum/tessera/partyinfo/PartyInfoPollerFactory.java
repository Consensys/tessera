package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface PartyInfoPollerFactory {

    static PartyInfoPollerFactory newFactory(Config config) {

        List<PartyInfoPollerFactory> list = new ArrayList<>();
        ServiceLoader<PartyInfoPollerFactory> loader = ServiceLoader.load(PartyInfoPollerFactory.class);
        loader.iterator().forEachRemaining(list::add);

        return list.stream()
                .filter(f -> f.communicationType() == config.getP2PServerConfig().getCommunicationType())
                .findAny()
                .get();

    }

    CommunicationType communicationType();

    default PartyInfoPoller create(PartyInfoService partyInfoService,Config config) {
        P2pClient p2pClient = P2pClientFactory.newFactory(config).create(config);

        return new P2pClientPartyInfoPoller(partyInfoService, p2pClient);
    }

}
