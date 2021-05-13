package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.partyinfo.model.Party;
import java.util.*;

/** An in-memory store of outstanding parties to contact for transaction resending */
public class ResendPartyStoreImpl implements ResendPartyStore {

  private Set<Party> allSeenParties;

  private Queue<SyncableParty> outstandingParties;

  public ResendPartyStoreImpl() {
    this.outstandingParties = new LinkedList<>();
    this.allSeenParties = new HashSet<>();
  }

  @Override
  public void addUnseenParties(final Collection<Party> partiesToRequestFrom) {
    final Set<Party> knownParties = new HashSet<>(partiesToRequestFrom);
    knownParties.removeAll(allSeenParties);
    this.allSeenParties.addAll(knownParties);

    knownParties.stream()
        .map(party -> new SyncableParty(party, 0))
        .forEach(outstandingParties::add);
  }

  @Override
  public Optional<SyncableParty> getNextParty() {
    return Optional.ofNullable(this.outstandingParties.poll());
  }

  @Override
  public void incrementFailedAttempt(final SyncableParty attemptedParty) {
    if (attemptedParty.getAttempts() < MAX_ATTEMPTS) {
      final SyncableParty updatedParty =
          new SyncableParty(attemptedParty.getParty(), attemptedParty.getAttempts() + 1);

      this.outstandingParties.add(updatedParty);
    }
  }
}
