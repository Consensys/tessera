package com.quorum.tessera.p2p;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

enum SimplePartyStore implements PartyStore {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePartyStore.class);

    private final SortedSet<URI> parties = new ConcurrentSkipListSet<>();

    @Override
    public Set<URI> getParties() {
        LOGGER.debug("Fetching parties {}",parties.stream()
            .map(Objects::toString)
            .collect(Collectors.joining(",")));

        return Set.copyOf(parties);
    }

    @Override
    public PartyStore store(URI party) {
        LOGGER.debug("Store {}",party);
        parties.add(party);
        return this;
    }

    @Override
    public PartyStore remove(URI party) {
        LOGGER.debug("Remove {}",party);
        parties.remove(party);
        return this;
    }
}
