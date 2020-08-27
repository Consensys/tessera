package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.version.ApiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DiscoveryHelperImpl implements DiscoveryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryHelperImpl.class);

    private Enclave enclave;

    private final NetworkStore networkStore;


    public DiscoveryHelperImpl(NetworkStore networkStore,Enclave enclave) {
        this.networkStore = networkStore;
        this.enclave = enclave;
    }

    @Override
    public NodeInfo buildCurrent() {

        final URI uri = RuntimeContext.getInstance().getP2pServerUri();
        final NodeUri nodeUri = NodeUri.create(uri);
        final List<ActiveNode> activeNodes = networkStore.getActiveNodes()
            .collect(Collectors.toList());

        Set<Recipient> recipients = activeNodes.stream()
            //  .filter(a -> a.getUri().equals(nodeUri))
            .filter(a -> !a.getKeys().isEmpty())
            .flatMap(a -> a.getKeys().stream()
                .map(k -> Recipient.of(k,a.getUri().asString()))
            ).collect(Collectors.toSet());

        Set<Party> parties = activeNodes.stream()
            .map(ActiveNode::getUri)
            .map(NodeUri::asString)
            .map(Party::new)
            .collect(Collectors.toSet());

        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withParties(parties)
            .withRecipients(recipients)
            .withUrl(nodeUri.asString())
            .withSupportedApiVersions(ApiVersion.versions())
            .build();

        LOGGER.debug("Built nodeinfo {}",nodeInfo);
        return nodeInfo;
    }

    @Override
    public void onCreate() {
        RuntimeContext runtimeContext = RuntimeContext.getInstance();
        runtimeContext.getPeers().stream()
            .map(NodeUri::create)
            .map(ActiveNode.Builder.create()::withUri)
            .map(ActiveNode.Builder::build)
            .forEach(networkStore::store);

        final NodeUri nodeUri = Optional.of(runtimeContext)
            .map(RuntimeContext::getP2pServerUri)
            .map(NodeUri::create)
            .get();

        ActiveNode thisNode = ActiveNode.Builder.create()
            .withUri(nodeUri)
            .withKeys(enclave.getPublicKeys())
            .withUri(nodeUri)
            .withSupportedVersions(ApiVersion.versions())
            .build();

        networkStore.store(thisNode);
    }

}


