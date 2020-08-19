package com.quorum.tessera.p2p;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.discovery.PartyStore;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 */
public class PartyPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyPoller.class);

    private final P2pClient p2pClient;

    private final PartyStore partyStore;

    private final Discovery discovery;

    public PartyPoller(P2pClient p2pClient) {
        this(PartyStore.getInstance(),p2pClient,Discovery.getInstance());
    }

    protected PartyPoller(PartyStore partyStore,P2pClient p2pClient,Discovery discovery) {
        this.partyStore = Objects.requireNonNull(partyStore);
        this.p2pClient = Objects.requireNonNull(p2pClient);
        this.discovery = Objects.requireNonNull(discovery);
    }

    public void onCreate() {
        LOGGER.debug("Enter onCreate[{}]",this);
        final NodeInfo nodeInfo = discovery.getCurrent();
        nodeInfo.getParties().stream()
            .map(Party::getUrl)
            .map(NodeUri::create)
            .map(NodeUri::asURI)
            .forEach(partyStore::store);
        LOGGER.debug("Exit onCreate[{}]",this);
    }

    @Override
    public void run() {

        final NodeInfo nodeInfo = discovery.getCurrent();

        nodeInfo.getParties().stream()
            .limit(20)//TODO : See what best number is
            .map(Party::getUrl)
            .map(NodeUri::create)
            .map(NodeUri::asURI)
            .forEach(u -> {
                try {
                    p2pClient.getParties(u)
                        .map(Party::getUrl)
                        .map(NodeUri::create)
                        .map(NodeUri::asURI)
                        .forEach(uri -> {
                            LOGGER.debug("Storing [{}] from {}",uri,u);
                            partyStore.store(uri);
                            LOGGER.debug("Stored [{}] from {}",uri,u);
                            //partyStore.remove(u);
                        });
                } catch (javax.ws.rs.ProcessingException ex) {
                    LOGGER.debug("Unable to connect to {}",u);
                    LOGGER.trace("",ex);
                }

        });
    }
}
