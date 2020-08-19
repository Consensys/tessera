package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.version.ApiVersion;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public interface Discovery {

    default void onCreate() {
        OnCreateHelper onCreateHelper = OnCreateHelper.getInstance();
        onCreateHelper.onCreate();
    }

    void onUpdate(NodeInfo nodeInfo);

    default NodeInfo getCurrent() {

        final NodeUri nodeUri = NodeUri.create(RuntimeContext.getInstance().getP2pServerUri());
        final NetworkStore networkStore = NetworkStore.getInstance();
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

        return NodeInfo.Builder.create()
            .withParties(parties)
            .withRecipients(recipients)
            .withUrl(nodeUri.asString())
            .withSupportedApiVersions(ApiVersion.versions())
            .build();
    }


    static Discovery getInstance() {
        return ServiceLoader.load(Discovery.class).findFirst().get();
    }



}
