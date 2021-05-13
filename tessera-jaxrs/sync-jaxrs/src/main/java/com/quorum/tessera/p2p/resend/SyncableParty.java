package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.partyinfo.model.Party;

/**
 * A SyncableParty is a {@link Party} that is to be contacted for transaction synchronisation. The
 * number of contact attempts is stored alongside the party.
 */
public class SyncableParty {

  private final Party party;

  private final int attempts;

  public SyncableParty(final Party party, final int attempts) {
    this.party = party;
    this.attempts = attempts;
  }

  public Party getParty() {
    return party;
  }

  public int getAttempts() {
    return attempts;
  }
}
