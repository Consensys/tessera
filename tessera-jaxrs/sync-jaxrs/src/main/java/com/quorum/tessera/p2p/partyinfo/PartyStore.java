package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.NodeUri;
import java.net.URI;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Support legacy collation of all parties to be added to
 * party info responses so nodes learn of nodes.
 */
public interface PartyStore {

  default void loadFromConfigIfEmpty() {
    RuntimeContext runtimeContext = RuntimeContext.getInstance();

    final Set<URI> parties = getParties();

    final URI ownUri = NodeUri.create(runtimeContext.getP2pServerUri()).asURI();

    final Set<URI> peerList =
        runtimeContext.getPeers().stream()
            .map(NodeUri::create)
            .map(NodeUri::asURI)
            .filter(p -> !p.equals(ownUri))
            .collect(Collectors.toUnmodifiableSet());

    if (parties.isEmpty() || !peerList.stream().anyMatch(parties::contains)) {
      peerList.forEach(this::store);
    }
  }

  Set<URI> getParties();

  PartyStore store(URI party);

  PartyStore remove(URI party);

  static PartyStore getInstance() {
    return ServiceLoader.load(PartyStore.class).findFirst().get();
  }
}
