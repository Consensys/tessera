package com.quorum.tessera.p2p.resend;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.partyinfo.model.Party;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class ResendPartyStoreTest {

  private ResendPartyStore resendPartyStore;

  @Before
  public void init() {
    this.resendPartyStore = ResendPartyStore.create();
  }

  @Test
  public void newPeersAreServed() {
    final List<Party> peers = Arrays.asList(new Party("newurl1.com"), new Party("newurl2.com"));

    this.resendPartyStore.addUnseenParties(peers);

    final Optional<SyncableParty> partyOne = resendPartyStore.getNextParty();
    final Optional<SyncableParty> partyTwo = resendPartyStore.getNextParty();

    assertThat(partyOne).isPresent();
    assertThat(partyTwo).isPresent();

    assertThat(partyOne.get().getAttempts()).isEqualTo(0);
    assertThat(partyTwo.get().getAttempts()).isEqualTo(0);

    final List<Party> resultList =
        Arrays.asList(partyOne.get().getParty(), partyTwo.get().getParty());
    assertThat(resultList)
        .containsExactlyInAnyOrder(new Party("newurl1.com"), new Party("newurl2.com"));

    final Optional<SyncableParty> partyThree = resendPartyStore.getNextParty();
    assertThat(partyThree).isNotPresent();
  }

  @Test
  public void oldPeersAreNotServed() {
    final List<Party> peers = singletonList(new Party("newurl1.com"));

    this.resendPartyStore.addUnseenParties(peers);

    final Optional<SyncableParty> partyOne = resendPartyStore.getNextParty();
    assertThat(partyOne).isPresent();
    assertThat(partyOne.get().getAttempts()).isEqualTo(0);
    assertThat(partyOne.get().getParty()).isEqualTo(new Party("newurl1.com"));

    final Optional<SyncableParty> partyTwo = resendPartyStore.getNextParty();
    assertThat(partyTwo).isNotPresent();

    this.resendPartyStore.addUnseenParties(peers);
    final Optional<SyncableParty> partyThree = resendPartyStore.getNextParty();
    assertThat(partyThree).isNotPresent();
  }

  @Test
  public void failedRequestMakesPartyAvailableForUseIfBelowThreeshold() {

    final int presetAttempts = 10;
    final Party party = new Party("badurl.com");
    final SyncableParty failedReq = new SyncableParty(party, presetAttempts);

    this.resendPartyStore.incrementFailedAttempt(failedReq);

    final Optional<SyncableParty> partyOne = resendPartyStore.getNextParty();
    assertThat(partyOne).isPresent();
    assertThat(partyOne.get().getParty()).isEqualTo(party);
    assertThat(partyOne.get().getAttempts()).isEqualTo(presetAttempts + 1);
  }

  @Test
  public void failedRequestDoesntMakePartyAvailableForUseIfAboveThreeshold() {

    final int presetAttempts = 25;
    final Party party = new Party("badurl.com");
    final SyncableParty failedReq = new SyncableParty(party, presetAttempts);

    this.resendPartyStore.incrementFailedAttempt(failedReq);

    final Optional<SyncableParty> partyOne = resendPartyStore.getNextParty();
    assertThat(partyOne).isNotPresent();
  }

  @Test
  public void failedRequestDoesntMakePartyAvailableForUseIfAtThreeshold() {

    final int presetAttempts = 20;
    final Party party = new Party("badurl.com");
    final SyncableParty failedReq = new SyncableParty(party, presetAttempts);

    this.resendPartyStore.incrementFailedAttempt(failedReq);

    final Optional<SyncableParty> partyOne = resendPartyStore.getNextParty();
    assertThat(partyOne).isNotPresent();
  }
}
