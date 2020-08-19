package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoDiscovery implements Discovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDiscovery.class);

    private final NetworkStore networkStore;

    private final Enclave enclave;

    public AutoDiscovery(NetworkStore networkStore, Enclave enclave) {
        this.networkStore = Objects.requireNonNull(networkStore);
        this.enclave = Objects.requireNonNull(enclave);
    }

    @Override
    public void onUpdate(final NodeInfo nodeInfo) {

        LOGGER.debug("Processing node info {}",nodeInfo);

        final NodeUri callerNodeUri = NodeUri.create(nodeInfo.getUrl());

        LOGGER.debug("Update node {}",callerNodeUri);

        final NodeInfo currentNodeInfo = getCurrent();

        final NodeInfo mergedNodeInfo = NodeInfo.Builder.from(currentNodeInfo)
            .withUrl(callerNodeUri.asString())
            .withParties(nodeInfo.getParties())
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


}
