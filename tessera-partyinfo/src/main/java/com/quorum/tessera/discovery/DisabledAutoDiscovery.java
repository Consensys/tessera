package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.AutoDiscoveryDisabledException;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DisabledAutoDiscovery implements Discovery {

    private final NetworkStore networkStore;

    private final Set<NodeUri> knownPeers;

    private final Enclave enclave;

    public DisabledAutoDiscovery(NetworkStore networkStore,Enclave enclave,Set<NodeUri> knownPeers) {
        this.networkStore = Objects.requireNonNull(networkStore);
        this.enclave = enclave;
        this.knownPeers = knownPeers;
    }


    @Override
    public void onUpdate(NodeInfo nodeInfo) {

        if(!knownPeers.contains(NodeUri.create(nodeInfo.getUrl()))) {
            throw new AutoDiscoveryDisabledException(String.format("%s is not a known peer", nodeInfo.getUrl()));
        }

        NodeUri callerNodeUri = NodeUri.create(nodeInfo.getUrl());

        Set<PublicKey> keys = nodeInfo.getRecipients().stream()
            .filter(r -> NodeUri.create(r.getUrl()).equals(callerNodeUri))
            .map(Recipient::getKey)
            .collect(Collectors.toSet());

        ActiveNode activeNode = ActiveNode.Builder.create()
            .withUri(callerNodeUri)
            .withSupportedVersions(nodeInfo.supportedApiVersions())
            .withKeys(keys)
            .build();

        networkStore.store(activeNode);

    }

}
