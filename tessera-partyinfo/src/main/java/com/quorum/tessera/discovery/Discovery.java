package com.quorum.tessera.discovery;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;

import java.net.URI;
import java.util.ServiceLoader;
import java.util.Set;

public interface Discovery {

    default void onCreate() {
        DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
        discoveryHelper.onCreate();
    }

    void onUpdate(NodeInfo nodeInfo);

    void onDisconnect(URI nodeUri);

    default NodeInfo getCurrent() {
        DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
        return discoveryHelper.buildCurrent();
    }

    default NodeInfo getRemoteNodeInfo(PublicKey publicKey) {
        DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
        return discoveryHelper.buildRemoteNodeInfo(publicKey);
    }

    default Set<NodeInfo> getRemoteNodeInfos() {
        DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
        return discoveryHelper.buildRemoteNodeInfos();
    }

    static Discovery getInstance() {
        return ServiceLoader.load(Discovery.class).findFirst().get();
    }
}
