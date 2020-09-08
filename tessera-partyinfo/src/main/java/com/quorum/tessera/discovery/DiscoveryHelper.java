package com.quorum.tessera.discovery;

import com.quorum.tessera.partyinfo.node.NodeInfo;

import java.util.ServiceLoader;

public interface DiscoveryHelper {

    NodeInfo buildCurrent();

    void onCreate();

    static DiscoveryHelper getInstance() {
        return ServiceLoader.load(DiscoveryHelper.class).findFirst().get();
    }

}
