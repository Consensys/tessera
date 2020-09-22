package com.quorum.tessera.p2p.partyinfo;


import com.quorum.tessera.discovery.NodeUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

enum SimplePartyStore implements PartyStore {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePartyStore.class);

    private final SortedSet<URI> parties = new ConcurrentSkipListSet<>();

    @Override
    public Set<URI> getParties() {
        LOGGER.debug("Fetching parties {}",Objects.toString(parties));

        return Set.copyOf(parties);
    }

    @Override
    public PartyStore store(URI party) {
        NodeUri nodeUri = NodeUri.create(party);
        LOGGER.debug("Store {}",nodeUri.asURI());
        parties.add(nodeUri.asURI());
        return this;
    }

    @Override
    public PartyStore remove(URI party) {
        NodeUri nodeUri = NodeUri.create(party);
        LOGGER.debug("Remove {}",nodeUri.asURI());
        parties.remove(nodeUri.asURI());
        return this;
    }
}
