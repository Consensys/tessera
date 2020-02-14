package com.quorum.tessera.partyinfo;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.util.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartyInfoServiceTest {

    private static final String LOCALHOST_8080 = "http://localhost:8080";

    private PartyInfoStore partyInfoStore;

    private MockRuntimeContext runtimeContext;

    private Enclave enclave;

    private PartyInfoServiceImpl partyInfoService;

    private PayloadPublisher payloadPublisher;

    private static final MockRuntimeContext mockRuntimeContextFactory =
            (MockRuntimeContext) RuntimeContextFactory.newFactory().create(new Config());

    PartyInfo storedPartyInfo = mock(PartyInfo.class);

    @Before
    public void createFixturesAndPopulateStore() throws Exception {

        runtimeContext =
                mockRuntimeContextFactory
                        .setP2pServerUri(URI.create(LOCALHOST_8080))
                        .setPeers(Arrays.asList(URI.create("http://other-node.com:8080")))
                        .setRemoteKeyValidation(true)
                        .setDisablePeerDiscovery(false);

        this.partyInfoStore = mock(PartyInfoStore.class);
        this.enclave = mock(Enclave.class);
        this.payloadPublisher = mock(PayloadPublisher.class);
        //

        final Set<PublicKey> ourKeys =
                Set.of(PublicKey.from("some-key".getBytes()), PublicKey.from("another-public-key".getBytes()));

        when(enclave.getPublicKeys()).thenReturn(ourKeys);
        storedPartyInfo = mock(PartyInfo.class);
        when(storedPartyInfo.getUrl()).thenReturn(LOCALHOST_8080);
        when(partyInfoStore.getPartyInfo()).thenReturn(storedPartyInfo);

        this.partyInfoService = new PartyInfoServiceImpl(partyInfoStore, enclave, payloadPublisher);
        partyInfoService.populateStore();

        verify(enclave).getPublicKeys();
        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore).store(any(PartyInfo.class));

        verifyNoMoreInteractions(partyInfoStore, enclave, payloadPublisher);
        // Clear out invocations after ths point
        reset(partyInfoStore, enclave, payloadPublisher);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(partyInfoStore);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(payloadPublisher);
    }

    @Test
    public void doNothing() {
        verifyZeroInteractions(partyInfoStore, enclave, payloadPublisher);
    }

    @Test
    public void autoDiscoveryAsEnabledStoresAsIs() {

        final PartyInfo incomingPartyInfo = mock(PartyInfo.class);
        final PartyInfo outgoingPartyInfo = mock(PartyInfo.class);

        runtimeContext.setRemoteKeyValidation(true).setDisablePeerDiscovery(false);

        when(partyInfoStore.getPartyInfo()).thenReturn(outgoingPartyInfo);

        final PartyInfo result = this.partyInfoService.updatePartyInfo(incomingPartyInfo);

        assertThat(result).isSameAs(outgoingPartyInfo);

        verify(partyInfoStore).store(any(PartyInfo.class));
        verify(partyInfoStore).getPartyInfo();
    }

    @Test
    public void autoDiscoveryDisabledUnknownPeer() {

        runtimeContext.setDisablePeerDiscovery(true).setRemoteKeyValidation(true);

        final PartyInfo forUpdate = new PartyInfo("SomeUnknownUri", emptySet(), emptySet());

        final Throwable throwable = catchThrowable(() -> partyInfoService.updatePartyInfo(forUpdate));

        assertThat(throwable)
                .isInstanceOf(AutoDiscoveryDisabledException.class)
                .hasMessage("Peer SomeUnknownUri not found in known peer list");
    }

    @Ignore
    @Test
    public void autoDiscoveryDisabledOnlyKnownKeysAdded() {

        // given
        runtimeContext.setDisablePeerDiscovery(false).setRemoteKeyValidation(true);

        Map<String, String> recipientMap =
                new HashMap<>() {
                    {
                        put("known", URI.create("http://other-node.com:8080").toString());
                        put("unknown", URI.create("http://unknown.com:8080").toString());
                    }
                };

        Recipient known = new Recipient(PublicKey.from("known".getBytes()), recipientMap.get("known"));
        Recipient unknown = new Recipient(PublicKey.from("unknown".getBytes()), recipientMap.get("unknown"));

        final PartyInfo forUpdate = new PartyInfo(known.getUrl(), Set.of(known, unknown), emptySet());

        final PartyInfo existingPartyInfo = this.storedPartyInfo;

        when(partyInfoStore.getPartyInfo()).thenReturn(existingPartyInfo);
        partyInfoService.updatePartyInfo(forUpdate);

        assertThat(runtimeContext.getPeers()).containsOnly(URI.create(recipientMap.get("known")));

        // check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore, times(2)).store(captor.capture());

        final List<Recipient> allRegisteredKeys =
                captor.getAllValues().stream().map(PartyInfo::getRecipients).flatMap(Set::stream).collect(toList());

        assertThat(allRegisteredKeys)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        new Recipient(PublicKey.from("some-key".getBytes()), LOCALHOST_8080 + "/"),
                        new Recipient(PublicKey.from("another-public-key".getBytes()), LOCALHOST_8080 + "/"),
                        new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080"));
    }

    @Ignore
    @Test
    public void autoDiscoveryDisabledNoIncomingPeersAdded() {

        runtimeContext.setDisablePeerDiscovery(true).setRemoteKeyValidation(true);

        final Recipient known = new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080");
        final Recipient unknown = new Recipient(PublicKey.from("unknown".getBytes()), "http://unknown.com:8080");

        final PartyInfo forUpdate = new PartyInfo("http://other-node.com:8080", Set.of(known, unknown), emptySet());

        final PartyInfo existing = storedPartyInfo;

        when(partyInfoService.getPartyInfo()).thenReturn(existing);

        partyInfoService.updatePartyInfo(forUpdate);

        // check that the only added keys were from that node (and our own)
        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).getPartyInfo();
        verify(partyInfoStore, times(1)).store(captor.capture());

        final List<Recipient> allRegisteredKeys =
                captor.getAllValues().stream().map(PartyInfo::getRecipients).flatMap(Set::stream).collect(toList());

        assertThat(allRegisteredKeys)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        new Recipient(PublicKey.from("some-key".getBytes()), LOCALHOST_8080 + "/"),
                        new Recipient(PublicKey.from("another-public-key".getBytes()), LOCALHOST_8080 + "/"),
                        new Recipient(PublicKey.from("known".getBytes()), "http://other-node.com:8080"));
    }

    @Test
    public void removeRecipient() {
        final String uri = "foo.com";
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

        verify(enclave).getPublicKeys();
        verifyZeroInteractions(payloadPublisher);
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

        Set<Object> services = Set.of(mock(Enclave.class), mock(PayloadPublisher.class), mock(PartyInfoStore.class));

        MockServiceLocator.createMockServiceLocator().setServices(services);

        final PartyInfoServiceFactory factory = PartyInfoServiceFactory.create();

        assertThat(new PartyInfoServiceImpl(factory)).isNotNull();
    }

    @Test
    public void attemptToUpdateRecipientWithExistingKeyWithRemoteKeyValidationDisabled() {
        // setup services

        runtimeContext.setRemoteKeyValidation(false).setDisablePeerDiscovery(false);

        // setup data
        final String uri = "http://localhost:8080";

        final PublicKey testKey = PublicKey.from("some-key".getBytes());
        final PartyInfo initial = new PartyInfo(uri, singleton(new Recipient(testKey, uri)), emptySet());
        when(partyInfoStore.getPartyInfo()).thenReturn(initial);

        final PublicKey extraKey = PublicKey.from("some-other-key".getBytes());

        final Set<Recipient> newRecipients =
                new HashSet<>(
                        Arrays.asList(
                                new Recipient(testKey, "http://other.com"),
                                new Recipient(extraKey, "http://some-other-url.com")));
        final PartyInfo updated = new PartyInfo(uri, newRecipients, emptySet());

        // call it
        final PartyInfo updatedInfo = partyInfoService.updatePartyInfo(updated);

        // verify
        assertThat(updatedInfo.getRecipients()).hasSize(1).containsExactly(new Recipient(testKey, uri));

        verify(partyInfoStore, times(2)).getPartyInfo();
        verify(partyInfoStore).store(updated);
    }
}
