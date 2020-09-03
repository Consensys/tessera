package com.quorum.tessera.discovery;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoDiscovery implements Discovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDiscovery.class);

    private final NetworkStore networkStore;

    public AutoDiscovery(NetworkStore networkStore) {
        this.networkStore = Objects.requireNonNull(networkStore);
    }

    @Override
    public void onUpdate(final NodeInfo nodeInfo) {

        LOGGER.debug("Processing node info {}",nodeInfo);

        final NodeUri callerNodeUri = NodeUri.create(nodeInfo.getUrl());

        LOGGER.debug("Update node {}",callerNodeUri);

        final NodeInfo currentNodeInfo = getCurrent();

        final NodeInfo mergedNodeInfo = NodeInfo.Builder.from(currentNodeInfo)
            .withUrl(callerNodeUri.asString())
            .withRecipients(nodeInfo.getRecipients())
            .withSupportedApiVersions(nodeInfo.supportedApiVersions())
            .build();

        LOGGER.debug("Merged node info {}",mergedNodeInfo);

        final Set<PublicKey> keys = mergedNodeInfo.getRecipients().stream()
            .filter(r -> NodeUri.create(r.getUrl()).equals(callerNodeUri))
            .map(Recipient::getKey)
            .collect(Collectors.toSet());

        final ActiveNode activeNode = ActiveNode.Builder.create()
            .withUri(callerNodeUri)
            .withSupportedVersions(nodeInfo.supportedApiVersions())
            .withKeys(keys)
            .build();

        networkStore.store(activeNode);
    }

    @Override
    public void onDisconnect(URI nodeUri) {
        networkStore.remove(NodeUri.create(nodeUri));
    }


}
