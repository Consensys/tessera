package com.quorum.tessera.p2p;

import java.net.URI;
import java.util.ServiceLoader;
import java.util.Set;

/*
 * Support legacy collation of all parties to be added to
 * party info reponses so nodes learn of nodes.
 */
public interface PartyStore {

    Set<URI> getParties();

    PartyStore store(URI party);

    PartyStore remove(URI party);

    static PartyStore getInstance() {
        return ServiceLoader.load(PartyStore.class).findFirst().get();
    }


}
