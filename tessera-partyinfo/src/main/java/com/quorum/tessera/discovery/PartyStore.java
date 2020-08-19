package com.quorum.tessera.discovery;

import java.net.URI;
import java.util.ServiceLoader;
import java.util.Set;

public interface PartyStore {

    Set<URI> getParties();

    PartyStore store(URI party);

    PartyStore remove(URI party);

    static PartyStore getInstance() {
        return ServiceLoader.load(PartyStore.class).findFirst().get();
    }


}
