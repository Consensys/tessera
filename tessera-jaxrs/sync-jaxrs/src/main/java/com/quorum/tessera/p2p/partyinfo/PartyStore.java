package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.NodeUri;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum PartyStore {
  INSTANCE;

  private static final Logger LOGGER = LoggerFactory.getLogger(PartyStore.class);

  private final SortedSet<URI> parties = new ConcurrentSkipListSet<>();

  public Set<URI> getParties() {
    LOGGER.debug("Fetching parties {}", Objects.toString(parties));

    return Set.copyOf(parties);
  }

  public void loadFromConfigIfEmpty() {
    RuntimeContext runtimeContext = RuntimeContext.getInstance();

    final Set<URI> parties = getParties();

    if (parties.isEmpty()
        || !runtimeContext.getPeers().stream()
            .map(NodeUri::create)
            .map(NodeUri::asURI)
            .anyMatch(parties::contains)) {
      runtimeContext.getPeers().forEach(this::store);
    }
  }

  public PartyStore store(URI party) {
    NodeUri nodeUri = NodeUri.create(party);
    LOGGER.debug("Store {}", nodeUri.asURI());
    parties.add(nodeUri.asURI());
    return this;
  }

  public PartyStore remove(URI party) {
    NodeUri nodeUri = NodeUri.create(party);
    LOGGER.debug("Remove {}", nodeUri.asURI());
    parties.remove(nodeUri.asURI());
    return this;
  }

  public static PartyStore getInstance() {
    return INSTANCE;
  }
}
