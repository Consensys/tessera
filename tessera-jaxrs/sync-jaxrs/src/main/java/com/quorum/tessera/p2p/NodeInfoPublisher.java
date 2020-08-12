package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.node.NodeInfo;

public interface NodeInfoPublisher {

    boolean publishNodeInfo(String targetUrl, NodeInfo existingNodeInfo);
}
