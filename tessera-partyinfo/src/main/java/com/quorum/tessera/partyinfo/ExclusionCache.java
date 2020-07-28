package com.quorum.tessera.partyinfo;

import java.util.Optional;
import java.util.ServiceLoader;

public interface ExclusionCache<T> {

    boolean isExcluded(T recipient);

    ExclusionCache<T> exclude(T recipient);

    Optional<T> include(String recipientUrl);

    static <T> ExclusionCache<T> create() {
        return ServiceLoader.load(ExclusionCache.class)
            .findFirst().get();
    }

}
