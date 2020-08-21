package com.quorum.tessera.discovery;

import com.quorum.tessera.partyinfo.node.NodeInfo;

import java.net.URI;
import java.util.ServiceLoader;

public interface Discovery {

    default void onCreate() {
        DiscoveryHelper discoveryHelper = DiscoveryHelper.getInstance();
        discoveryHelper.onCreate();
    }

    void onUpdate(NodeInfo nodeInfo);

    default NodeInfo getCurrent() {
        DiscoveryHelper discoveryHelper = DiscoveryHelper.getInstance();
        return discoveryHelper.buildCurrent();
    }

    void onDisconnect(URI nodeUri);

    static Discovery getInstance() {
        return ServiceLoader.load(Discovery.class).findFirst().get();
    }



}
