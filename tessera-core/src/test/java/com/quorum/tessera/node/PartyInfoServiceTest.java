package com.quorum.tessera.node;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PartyInfoServiceTest {

    private static final String URI = "http://localhost:8080";

    private PartyInfoStore partyInfoStore;

    private ConfigService configService;

    private Enclave enclave;

    private PartyInfoService partyInfoService;

    @Before
    public void onSetUp() throws URISyntaxException {

        this.partyInfoStore = mock(PartyInfoStore.class);
        this.enclave = mock(Enclave.class);
        this.configService = mock(ConfigService.class);

        doReturn(new URI(URI)).when(configService).getServerUri();

        final Peer peer = new Peer("http://other-node.com:8080");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final Set<PublicKey> ourKeys = new HashSet<>(
            Arrays.asList(
                PublicKey.from("some-key".getBytes()),
                PublicKey.from("another-public-key".getBytes())
            )
        );
        doReturn(ourKeys).when(enclave).getPublicKeys();

        this.partyInfoService = new PartyInfoServiceImpl(partyInfoStore, configService, enclave);
    }

    @After
    public void after() {
        //Called in constructor
        verify(enclave).getPublicKeys();
        verify(configService).getServerUri();
        verify(configService,atLeast(1)).getPeers();
        verify(partyInfoStore, atLeast(1)).store(any(PartyInfo.class));

        verifyNoMoreInteractions(partyInfoStore);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(configService);
    }

    @Test
    public void registeringPublicKeysUsesOurUrl() {

        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).store(captor.capture());
        verify(enclave).getPublicKeys();

        final List<Recipient> allRegisteredKeys = captor
            .getAllValues()
            .stream()
            .map(PartyInfo::getRecipients)
            .flatMap(Set::stream)
            .collect(toList());

        assertThat(allRegisteredKeys)
            .hasSize(2)
            .containsExactlyInAnyOrder(
                new Recipient(PublicKey.from("some-key".getBytes()), URI + "/"),
                new Recipient(PublicKey.from("another-public-key".getBytes()), URI + "/")
            );
    }

    @Test
    public void getRecipientURLFromPartyInfoStore() {

        final Recipient recipient = new Recipient(PublicKey.from("key".getBytes()), "someurl");
        final PartyInfo partyInfo = new PartyInfo(URI, singleton(recipient), emptySet());
        doReturn(partyInfo).when(partyInfoStore).getPartyInfo();

        final String result = partyInfoService.getURLFromRecipientKey(PublicKey.from("key".getBytes()));
        assertThat(result).isEqualTo("someurl");

        verify(partyInfoStore).getPartyInfo();
    }

    @Test
    public void getRecipientURLFromPartyInfoStoreFailsIfKeyDoesntExist() {

        doReturn(new PartyInfo("", emptySet(), emptySet())).when(partyInfoStore).getPartyInfo();

        final PublicKey failingKey = PublicKey.from("otherKey".getBytes());
        final Throwable throwable = catchThrowable(() -> partyInfoService.getURLFromRecipientKey(failingKey));
        assertThat(throwable).isInstanceOf(KeyNotFoundException.class).hasMessage("Recipient not found for key: "+ failingKey.encodeToBase64());

        verify(partyInfoStore).getPartyInfo();
    }

    @Test
    public void autoDiscoveryEnabledStoresAsIs() {

        final PartyInfo incomingPartyInfo = mock(PartyInfo.class);
        final PartyInfo outgoingPartyInfo = mock(PartyInfo.class);

        when(configService.isDisablePeerDiscovery()).thenReturn(false);
        when(partyInfoStore.getPartyInfo()).thenReturn(outgoingPartyInfo);

        final PartyInfo result = this.partyInfoService.updatePartyInfo(incomingPartyInfo);

        assertThat(result).isSameAs(outgoingPartyInfo);

        verify(partyInfoStore, times(2)).store(any(PartyInfo.class));
        verify(partyInfoStore).getPartyInfo();
        verify(configService).isDisablePeerDiscovery();
    }

    @Test
    public void autoDiscoveryDisabledUnknownPeer() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        final PartyInfo forUpdate = new PartyInfo("SomeUnknownUri", emptySet(), emptySet());

        final Throwable throwable = catchThrowable(() -> partyInfoService.updatePartyInfo(forUpdate));

        assertThat(throwable)
            .isInstanceOf(AutoDiscoveryDisabledException.class)
            .hasMessage("Peer SomeUnknownUri not found in known peer list");

        verify(configService).isDisablePeerDiscovery();

    }

    @Test
    public void autoDiscoveryDisabledOnlyKnownKeysAdded() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        Recipient known = new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080");
        Recipient unknown = new Recipient(PublicKey.from("unknown".getBytes()), "http://unknown.com:8080");

        final PartyInfo forUpdate = new PartyInfo(
            "http://other-node.com:8080",
            Stream.of(known, unknown).collect(toSet()),
            emptySet()
        );

        partyInfoService.updatePartyInfo(forUpdate);


        verify(configService).isDisablePeerDiscovery();
        verify(configService,times(2)).getPeers();

        //check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore, times(2)).store(captor.capture());

        final List<Recipient> allRegisteredKeys = captor
            .getAllValues()
            .stream()
            .map(PartyInfo::getRecipients)
            .flatMap(Set::stream)
            .collect(toList());

        assertThat(allRegisteredKeys)
            .hasSize(3)
            .containsExactlyInAnyOrder(
                new Recipient(PublicKey.from("some-key".getBytes()), URI + "/"),
                new Recipient(PublicKey.from("another-public-key".getBytes()), URI + "/"),
                new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080")
            );

    }

    @Test
    public void autoDiscoveryDisabledNoIncomingPeersAdded() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        final PartyInfo forUpdate = new PartyInfo(
            "http://other-node.com:8080",
            emptySet(),
            Stream.of(new Party("known"), new Party("unknown")).collect(toSet())
        );


        partyInfoService.updatePartyInfo(forUpdate);

        verify(configService).isDisablePeerDiscovery();
        verify(configService,times(2)).getPeers();

        //check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore,times(2)).store(captor.capture());


    }

}
