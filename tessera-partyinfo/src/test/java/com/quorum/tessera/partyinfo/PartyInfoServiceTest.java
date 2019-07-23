package com.quorum.tessera.partyinfo;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartyInfoServiceTest {

    private static final String URI = "http://localhost:8080";

    private PartyInfoStore partyInfoStore;

    private ConfigService configService;

    private Enclave enclave;

    private PartyInfoService partyInfoService;

    private PayloadPublisher payloadPublisher;

    @Before
    public void onSetUp() throws URISyntaxException {

        this.partyInfoStore = mock(PartyInfoStore.class);
        this.enclave = mock(Enclave.class);
        this.configService = mock(ConfigService.class);
        this.payloadPublisher = mock(PayloadPublisher.class);

        doReturn(new URI(URI)).when(configService).getServerUri();

        final Peer peer = new Peer("http://other-node.com:8080");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final Set<PublicKey> ourKeys =
                new HashSet<>(
                        Arrays.asList(
                                PublicKey.from("some-key".getBytes()),
                                PublicKey.from("another-public-key".getBytes())));
        doReturn(ourKeys).when(enclave).getPublicKeys();

        this.partyInfoService = new PartyInfoServiceImpl(partyInfoStore, configService, enclave, payloadPublisher);
    }

    @After
    public void after() {
        // Called in constructor
        verify(enclave, atLeast(1)).getPublicKeys();
        verify(configService).getServerUri();
        verify(configService, atLeast(1)).getPeers();
        verify(partyInfoStore, atLeast(1)).store(any(PartyInfo.class));

        verifyNoMoreInteractions(partyInfoStore);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(configService);
        verifyNoMoreInteractions(payloadPublisher);
    }

    @Test
    public void registeringPublicKeysUsesOurUrl() {

        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).store(captor.capture());
        verify(enclave, atLeast(1)).getPublicKeys();

        final List<Recipient> allRegisteredKeys =
                captor.getAllValues().stream().map(PartyInfo::getRecipients).flatMap(Set::stream).collect(toList());

        assertThat(allRegisteredKeys)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new Recipient(PublicKey.from("some-key".getBytes()), URI + "/"),
                        new Recipient(PublicKey.from("another-public-key".getBytes()), URI + "/"));
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

        final PartyInfo forUpdate =
                new PartyInfo("http://other-node.com:8080", Stream.of(known, unknown).collect(toSet()), emptySet());

        partyInfoService.updatePartyInfo(forUpdate);

        verify(configService).isDisablePeerDiscovery();
        verify(configService, times(2)).getPeers();

        // check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore, times(2)).store(captor.capture());

        final List<Recipient> allRegisteredKeys =
                captor.getAllValues().stream().map(PartyInfo::getRecipients).flatMap(Set::stream).collect(toList());

        assertThat(allRegisteredKeys)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        new Recipient(PublicKey.from("some-key".getBytes()), URI + "/"),
                        new Recipient(PublicKey.from("another-public-key".getBytes()), URI + "/"),
                        new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080"));
    }

    @Test
    public void autoDiscoveryDisabledNoIncomingPeersAdded() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        final PartyInfo forUpdate =
                new PartyInfo(
                        "http://other-node.com:8080",
                        emptySet(),
                        Stream.of(new Party("known"), new Party("unknown")).collect(toSet()));

        partyInfoService.updatePartyInfo(forUpdate);

        verify(configService).isDisablePeerDiscovery();
        verify(configService, times(2)).getPeers();

        // check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore, times(2)).store(captor.capture());
    }

    @Test
    public void removeRecipient() {

        String uri = "foo.com";
        partyInfoService.removeRecipient(uri);

        verify(partyInfoStore).removeRecipient(uri);
    }

    @Test
    public void publishPayload() {

        when(enclave.getPublicKeys()).thenReturn(singleton(PublicKey.from("Key Data".getBytes())));

        PublicKey recipientKey = PublicKey.from("Some Key Data".getBytes());

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getRecipients()).thenReturn(singleton(new Recipient(recipientKey, "http://somehost.com")));
        when(partyInfoStore.getPartyInfo()).thenReturn(partyInfo);

        EncodedPayload payload = mock(EncodedPayload.class);

        partyInfoService.publishPayload(payload, recipientKey);

        verify(payloadPublisher).publishPayload(payload, "http://somehost.com");
        verify(partyInfoStore).getPartyInfo();
    }

    @Test
    public void publishPayloadDoesntPublishToSender() {

        PublicKey recipientKey = PublicKey.from("Some Key Data".getBytes());

        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));

        EncodedPayload payload = mock(EncodedPayload.class);

        partyInfoService.publishPayload(payload, recipientKey);

        verifyZeroInteractions(payloadPublisher);
    }

    @Test
    public void publishPayloadKeyNotFound() {
        when(enclave.getPublicKeys()).thenReturn(singleton(PublicKey.from("Key Data".getBytes())));

        PublicKey recipientKey = PublicKey.from("Some Key Data".getBytes());

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getRecipients()).thenReturn(Collections.EMPTY_SET);
        when(partyInfoStore.getPartyInfo()).thenReturn(partyInfo);

        EncodedPayload payload = mock(EncodedPayload.class);

        try {
            partyInfoService.publishPayload(payload, recipientKey);
            failBecauseExceptionWasNotThrown(KeyNotFoundException.class);
        } catch (KeyNotFoundException ex) {
            verifyZeroInteractions(payloadPublisher);
            verify(partyInfoStore).getPartyInfo();
        }
    }

    @Test
    public void createWithDefaultConstructor() throws Exception {

        ConfigService configService = mock(ConfigService.class);
        when(configService.getServerUri()).thenReturn(new URI("bogus.com"));

        Set services =
                Stream.of(configService, mock(Enclave.class), mock(PayloadPublisher.class)).collect(Collectors.toSet());

        MockServiceLocator mockServiceLocator = MockServiceLocator.createMockServiceLocator();
        mockServiceLocator.setServices(services);

        assertThat(new PartyInfoServiceImpl()).isNotNull();
    }
}
