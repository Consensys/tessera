package com.quorum.tessera.partyinfo;

import java.util.ServiceLoader;

public interface ExclusionCache<T> {

    boolean isExcluded(T recipient);

    ExclusionCache<T> exclude(T recipient);

    static <T> ExclusionCache<T> create() {
        return ServiceLoader.load(ExclusionCache.class).findFirst().get();
    }

    ExclusionCache<T> start();

    void stop();


}
