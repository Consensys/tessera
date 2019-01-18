package com.quorum.tessera.test;

import java.util.stream.Stream;

public interface PartyHelper {

    Stream<Party> getParties();

    default Party findByAlias(String alias) {
        return getParties()
            .filter(p -> p.getAlias().equals(alias))
            .findAny()
            .orElseThrow(() -> new RuntimeException("No party found with alias " + alias));
    }

    default Party findByPublicKey(String publicKey) {
        return getParties()
            .filter(p -> p.getPublicKey().equals(publicKey))
            .findAny()
            .orElseThrow(() -> new RuntimeException("No party found with publicKey " + publicKey));

    }

    

}
