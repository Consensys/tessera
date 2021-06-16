package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.partyinfo.model.Party;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Stores nodes that need to be contacted for resending existing transactions
 *
 * <p>Also stores auxiliary information such as the number of attempts already tried for that node
 */
public interface ResendPartyStore {

  int MAX_ATTEMPTS = 20;

  /**
   * Adds a new set of parties to the list of all parties that are outstanding to be contacted.
   *
   * <p>Sets the number of attempts for each party to 0, even if the party already exists in the
   * list with a higher attempt count.
   *
   * <p>Duplicates are allowed and will be treated individually when making requests. i.e. adding
   * the same url twice will allow twice the limit of maximum attempts
   *
   * @param partiesToRequestFrom the parties to add to the contact list
   */
  void addUnseenParties(Collection<Party> partiesToRequestFrom);

  /**
   * Retrieves the next party to contact in the queue
   *
   * @return Returns {@link Optional#empty()} if there are no parties to contact in the request
   *     queue else an Optional containing a party to contact
   */
  Optional<SyncableParty> getNextParty();

  /**
   * Submit a party that had been attempted to contact but failed This will increase its attempt
   * count or discard it if the maximum number of attempts have been reached
   *
   * @param attemptedParty the party that has failed to be contacted
   */
  void incrementFailedAttempt(SyncableParty attemptedParty);

  static ResendPartyStore create() {
    return ServiceLoader.load(ResendPartyStore.class).findFirst().get();
  }
}
