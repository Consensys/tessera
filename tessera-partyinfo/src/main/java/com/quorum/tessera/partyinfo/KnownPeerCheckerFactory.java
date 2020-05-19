package com.quorum.tessera.partyinfo;

import java.util.Set;

public class KnownPeerCheckerFactory {

    public KnownPeerChecker create(Set<String> peers) {
        return new KnownPeerChecker(peers);
    }

}
