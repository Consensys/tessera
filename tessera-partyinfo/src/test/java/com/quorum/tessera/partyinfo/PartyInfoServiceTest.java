package com.quorum.tessera.partyinfo;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.Config;

import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class PartyInfoServiceTest {

    private static final String URI = "http://localhost:8080";

    private PartyInfoStore partyInfoStore;

    //Need to look at holding as singleton
    private final static MockRuntimeContext RUNTIME_CONTEXT =
        (MockRuntimeContext) RuntimeContextFactory.newFactory().create(mock(Config.class));

    private Enclave enclave;

    private PartyInfoServiceImpl partyInfoService;

    private PayloadPublisher payloadPublisher;

    @Before
    public void onSetUp() throws URISyntaxException {

        this.partyInfoStore = mock(PartyInfoStore.class);
        this.enclave = mock(Enclave.class);
        this.payloadPublisher = mock(PayloadPublisher.class);

        RUNTIME_CONTEXT
            .setP2pServerUri(java.net.URI.create(URI))
            .setPeers(singletonList(java.net.URI.create("http://other-node.com:8080")))
            .setRemoteKeyValidation(true);


        this.partyInfoService = new PartyInfoServiceImpl(partyInfoStore, enclave, payloadPublisher);

        verifyNoMoreInteractions(partyInfoStore);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(payloadPublisher);

        PartyInfo storedPartyInfo = mock(PartyInfo.class);
        when(storedPartyInfo.getUrl()).thenReturn(URI);

        when(partyInfoStore.getPartyInfo()).thenReturn(storedPartyInfo);

        final Set<PublicKey> ourKeys =
                Set.of(
                    PublicKey.from("some-key".getBytes()),
                    PublicKey.from("another-public-key".getBytes())
                );

        when(enclave.getPublicKeys()).thenReturn(ourKeys);

        partyInfoService.populateStore();

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore).store(any(PartyInfo.class));
        verify(enclave).getPublicKeys();

        verifyNoMoreInteractions(partyInfoStore);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(payloadPublisher);

        reset(partyInfoStore,enclave,payloadPublisher);
    }

    @After
    public void after() {

        verifyNoMoreInteractions(partyInfoStore);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(payloadPublisher);
    }


    @Test
    public void autoDiscoveryEnabledStoresAsIs() {

        RUNTIME_CONTEXT.setDisablePeerDiscovery(false);

        final PartyInfo incomingPartyInfo = mock(PartyInfo.class);
        final PartyInfo outgoingPartyInfo = mock(PartyInfo.class);

        when(partyInfoStore.getPartyInfo()).thenReturn(outgoingPartyInfo);

        final PartyInfo result = this.partyInfoService.updatePartyInfo(incomingPartyInfo);

        assertThat(result).isSameAs(outgoingPartyInfo);

        verify(partyInfoStore).store(any(PartyInfo.class));
        verify(partyInfoStore).getPartyInfo();
    }

    @Test
    public void autoDiscoveryDisabledUnknownPeer() {

        RUNTIME_CONTEXT.setDisablePeerDiscovery(true);

        final PartyInfo forUpdate = new PartyInfo("SomeUnknownUri", emptySet(), emptySet());

        final Throwable throwable = catchThrowable(() -> partyInfoService.updatePartyInfo(forUpdate));

        assertThat(throwable)
            .isInstanceOf(AutoDiscoveryDisabledException.class)
            .hasMessage("Peer SomeUnknownUri not found in known peer list");

    }

    @Test
    public void autoDiscoveryDisabledOnlyKnownKeysAdded() {

        RUNTIME_CONTEXT
            .setDisablePeerDiscovery(true);

        Recipient known = new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080");
        Recipient unknown = new Recipient(PublicKey.from("unknown".getBytes()), "http://unknown.com:8080");

        final PartyInfo forUpdate =
            new PartyInfo("http://other-node.com:8080", Set.of(known, unknown), emptySet());

        assertThat(forUpdate.getRecipients()).hasSize(2);

        // check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        partyInfoService.updatePartyInfo(forUpdate);

        verify(partyInfoStore).store(captor.capture());

        final List<Recipient> allRegisteredKeys =
            captor.getAllValues().stream()
                .map(PartyInfo::getRecipients)
                .flatMap(Set::stream)
                .collect(toList());

        assertThat(allRegisteredKeys)
            .hasSize(3)
            .containsExactlyInAnyOrder(
                new Recipient(PublicKey.from("some-key".getBytes()), URI + "/"),
                new Recipient(PublicKey.from("another-public-key".getBytes()), URI + "/"),
                new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080"));


        verify(partyInfoStore).getPartyInfo();

    }

    @Test
    public void autoDiscoveryDisabledNoIncomingPeersAdded() {

        RUNTIME_CONTEXT.setDisablePeerDiscovery(true);

        final PartyInfo forUpdate =
            new PartyInfo(
                "http://other-node.com:8080",
                emptySet(),
                Stream.of(new Party("known"), new Party("unknown")).collect(toSet()));

        partyInfoService.updatePartyInfo(forUpdate);

        // check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore).store(captor.capture());
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
        verify(enclave).getPublicKeys();
    }

    @Test
    public void publishPayloadDoesntPublishToSender() {

        PublicKey recipientKey = PublicKey.from("Some Key Data".getBytes());

        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));

        EncodedPayload payload = mock(EncodedPayload.class);

        partyInfoService.publishPayload(payload, recipientKey);

        verifyZeroInteractions(payloadPublisher);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void publishPayloadKeyNotFound() {
        when(enclave.getPublicKeys()).thenReturn(singleton(PublicKey.from("Key Data".getBytes())));

        PublicKey recipientKey = PublicKey.from("Some Key Data".getBytes());

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getRecipients()).thenReturn(Collections.emptySet());
        when(partyInfoStore.getPartyInfo()).thenReturn(partyInfo);

        EncodedPayload payload = mock(EncodedPayload.class);

        try {
            partyInfoService.publishPayload(payload, recipientKey);
            failBecauseExceptionWasNotThrown(KeyNotFoundException.class);
        } catch (KeyNotFoundException ex) {
            verifyZeroInteractions(payloadPublisher);
            verify(partyInfoStore).getPartyInfo();
            verify(enclave).getPublicKeys();
        }
    }

    @Test
    public void createWithFactoryConstructor() throws Exception {

        Set<Object> services =
            Stream.of(mock(Enclave.class), mock(PayloadPublisher.class), mock(PartyInfoStore.class))
                .collect(Collectors.toSet());

        MockServiceLocator.createMockServiceLocator().setServices(services);

        final PartyInfoServiceFactory factory = PartyInfoServiceFactory.create();

        assertThat(new PartyInfoServiceImpl(factory)).isNotNull();
    }


    @Test
    public void attemptToUpdateRecipientWithExistingKeyWithNewUrlIfToggleDisabled() {
        // setup services
        RUNTIME_CONTEXT
            .setRemoteKeyValidation(false)
            .setDisablePeerDiscovery(false);

        // setup data
        final String uri = "http://localhost:8080";

        final PublicKey testKey = PublicKey.from("some-key".getBytes());
        final PartyInfo initial = new PartyInfo(uri, singleton(new Recipient(testKey, uri)), emptySet());
        when(partyInfoStore.getPartyInfo()).thenReturn(initial);

        final PublicKey extraKey = PublicKey.from("some-other-key".getBytes());

        final Set<Recipient> newRecipients =
                Set.of(
                    new Recipient(testKey, "http://other.com"),
                    new Recipient(extraKey, "http://some-other-url.com")
                );

        final PartyInfo updated = new PartyInfo(uri, newRecipients, emptySet());

        // call it
        final PartyInfo updatedInfo = partyInfoService.updatePartyInfo(updated);

        // verify
        assertThat(updatedInfo.getRecipients()).hasSize(1).containsExactly(new Recipient(testKey, uri));
        verify(partyInfoStore, times(2)).getPartyInfo();
        verify(partyInfoStore).store(any(PartyInfo.class));
    }


}
