package com.quorum.tessera.recovery;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.partyinfo.node.NodeInfo;

import java.net.URI;

public class MockDiscovery implements Discovery {
    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate(NodeInfo nodeInfo) {

    }

    @Override
    public NodeInfo getCurrent() {
        return null;
    }

    @Override
    public void onDisconnect(URI nodeUri) {

    }
}
