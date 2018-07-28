package com.quorum.tessera.sync;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.sync.model.SyncableParty;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResendPartyStoreTest {

    private ResendPartyStore resendPartyStore;

    @Before
    public void init() {

        final Config config = mock(Config.class);
        doReturn(emptyList()).when(config).getPeers();

        this.resendPartyStore = new ResendPartyStoreImpl(config);
    }

    @Test
    public void initialPeersAreServed() {
        final List<Peer> peers = Arrays.asList(new Peer("url1.com"), new Peer("url2.com"));

        final Config config = mock(Config.class);
        doReturn(peers).when(config).getPeers();

        this.resendPartyStore = new ResendPartyStoreImpl(config);

        final Optional<SyncableParty> partyOne = resendPartyStore.getNextParty();
        assertThat(partyOne).isPresent();
        assertThat(partyOne.get().getParty()).isEqualTo(new Party("url1.com"));
        assertThat(partyOne.get().getAttempts()).isEqualTo(0);

        final Optional<SyncableParty> partyTwo = resendPartyStore.getNextParty();
        assertThat(partyTwo).isPresent();
        assertThat(partyTwo.get().getParty()).isEqualTo(new Party("url2.com"));
        assertThat(partyTwo.get().getAttempts()).isEqualTo(0);

        final Optional<SyncableParty> partyThree = resendPartyStore.getNextParty();
        assertThat(partyThree).isNotPresent();
    }

    @Test
    public void newPeersAreServed() {
        final List<Party> peers = Arrays.asList(new Party("newurl1.com"), new Party("newurl2.com"));

        this.resendPartyStore.addUnseenParties(peers);

        final Optional<SyncableParty> partyOne = resendPartyStore.getNextParty();
        assertThat(partyOne).isPresent();
        assertThat(partyOne.get().getParty()).isEqualTo(new Party("newurl1.com"));
        assertThat(partyOne.get().getAttempts()).isEqualTo(0);

        final Optional<SyncableParty> partyTwo = resendPartyStore.getNextParty();
        assertThat(partyTwo).isPresent();
        assertThat(partyTwo.get().getParty()).isEqualTo(new Party("newurl2.com"));
        assertThat(partyTwo.get().getAttempts()).isEqualTo(0);

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
