package com.quorum.tessera.p2p.partyinfo;

import java.net.URI;
import java.util.Set;

public class PartyStoreFactory implements PartyStore {

  public static PartyStore provider() {
    return SimplePartyStore.INSTANCE;
  }

  private final PartyStore delegate;

  public PartyStoreFactory() {
    this(provider());
  }

  protected PartyStoreFactory(PartyStore delegate) {
    this.delegate = delegate;
  }

  @Override
  public Set<URI> getParties() {
    return delegate.getParties();
  }

  @Override
  public PartyStore store(URI party) {
    return delegate.store(party);
  }

  @Override
  public PartyStore remove(URI party) {
    return delegate.remove(party);
  }
}
