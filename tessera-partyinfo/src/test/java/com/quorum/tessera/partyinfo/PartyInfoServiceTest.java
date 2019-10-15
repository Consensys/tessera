package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.config.FeatureToggles;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartyInfoServiceTest {

    private static final String URI = "http://localhost:8080";

    private ConfigService configService;

    private Enclave enclave;

    private PartyInfoServiceImpl partyInfoService;

    private PartyInfoStore partyInfoStore;

    private PartyInfoValidator partyInfoValidator;

    private PayloadEncoder payloadEncoder;

    @Before
    public void onSetUp() throws URISyntaxException {
        // TODO: Mock the store
        this.partyInfoStore = PartyInfoStore.create(java.net.URI.create(URI));
        this.enclave = mock(Enclave.class);
        this.configService = mock(ConfigService.class);
        this.partyInfoValidator = mock(PartyInfoValidator.class);
        this.payloadEncoder = mock(PayloadEncoder.class);
        doReturn(new URI(URI)).when(configService).getServerUri();

        final Peer peer = new Peer("http://other-node.com:8080");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final FeatureToggles featureToggles = new FeatureToggles();
        featureToggles.setEnableRemoteKeyValidation(true);
        when(configService.featureToggles()).thenReturn(featureToggles);

        final Set<PublicKey> ourKeys =
                new HashSet<>(
                        Arrays.asList(
                                PublicKey.from("some-key".getBytes()),
                                PublicKey.from("another-public-key".getBytes())));
        doReturn(ourKeys).when(enclave).getPublicKeys();

        this.partyInfoService =
                new PartyInfoServiceImpl(partyInfoStore, configService, enclave, partyInfoValidator, payloadEncoder);
    }

    @After
    public void after() {
        // Called in constructor

        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(configService);
        verifyNoMoreInteractions(partyInfoValidator);
        verifyNoMoreInteractions(payloadEncoder);
        partyInfoStore.clear();
    }

    @Test
    public void autoDiscoveryEnabledStoresAsIs() {

        final PartyInfo incomingPartyInfo = mock(PartyInfo.class);
        when(incomingPartyInfo.getUrl()).thenReturn(URI);

        final PartyInfo outgoingPartyInfo = partyInfoStore.getPartyInfo();

        when(configService.isDisablePeerDiscovery()).thenReturn(false);

        final PartyInfo result = this.partyInfoService.updatePartyInfo(incomingPartyInfo);

        assertThat(result.getUrl()).isEqualTo(outgoingPartyInfo.getUrl());

        verify(configService).isDisablePeerDiscovery();
        verify(configService).featureToggles();
    }

    @Test
    public void autoDiscoveryDisabledExceptionThrownForUnknownPeerUrl() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);
        FeatureToggles featureToggles = mock(FeatureToggles.class);
        when(featureToggles.isEnableRemoteKeyValidation()).thenReturn(false);

        when(configService.featureToggles()).thenReturn(featureToggles);

        Peer peer = mock(Peer.class);
        when(peer.getUrl()).thenReturn("http://unknown.com");

        when(configService.getPeers()).thenReturn(Collections.EMPTY_LIST);

        final PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn("http://other-node.com:8080");

        try {
            partyInfoService.updatePartyInfo(partyInfo);
            failBecauseExceptionWasNotThrown(AutoDiscoveryDisabledException.class);
        } catch (AutoDiscoveryDisabledException ex) {
            verify(configService).isDisablePeerDiscovery();
            verify(configService).featureToggles();
            verify(configService).getPeers();
        }
    }

    @Test
    public void autoDiscoveryDisabledNoIncomingPeersAdded() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        final PartyInfo forUpdate =
                new PartyInfo(
                        "http://other-node.com:8080",
                        emptySet(),
                        Stream.of(new Party("known"), new Party("unknown")).collect(toSet()));

        partyInfoService.onConstruct();

        partyInfoService.updatePartyInfo(forUpdate);

        verify(configService).isDisablePeerDiscovery();
        verify(configService, times(2)).getPeers();

        verify(configService).featureToggles();
        verify(enclave).getPublicKeys();
        verify(configService).getServerUri();
    }

    @Test
    public void attemptToUpdateRecipientWithExistingKeyWithNewUrlIsIgnoredIfToggleDisabled() {
        // setup services
        final FeatureToggles featureToggles = new FeatureToggles();
        featureToggles.setEnableRemoteKeyValidation(false);
        when(configService.featureToggles()).thenReturn(featureToggles);

        // setup data
        final String uri = "http://localhost:8080";

        final PublicKey testKey = PublicKey.from("some-key".getBytes());
        final PartyInfo initial = new PartyInfo(uri, singleton(new Recipient(testKey, uri)), emptySet());

        PartyInfoStoreImpl.INSTANCE.clear();
        PartyInfoStoreImpl.INSTANCE.store(initial);

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

        verify(configService).featureToggles();
    }

    @Test
    public void validateEmptyRecipientListsAsValid() {
        final String url = "http://somedomain.com";

        final Set<Recipient> existingRecipients = new HashSet<>();
        final Set<Recipient> newRecipients = new HashSet<>();
        final PartyInfo existingPartyInfo = new PartyInfo(url, existingRecipients, Collections.emptySet());
        final PartyInfo newPartyInfo = new PartyInfo(url, newRecipients, Collections.emptySet());

        assertThat(partyInfoService.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isTrue();
    }

    @Test
    public void validateAttemptToChangeUrlAsInvalid() {
        final String url = "http://somedomain.com";
        final PublicKey key = PublicKey.from("ONE".getBytes());

        final Set<Recipient> existingRecipients = Collections.singleton(new Recipient(key, "http://one.com"));
        final PartyInfo existingPartyInfo = new PartyInfo(url, existingRecipients, Collections.emptySet());

        final Set<Recipient> newRecipients = Collections.singleton(new Recipient(key, "http://two.com"));
        final PartyInfo newPartyInfo = new PartyInfo(url, newRecipients, Collections.emptySet());

        assertThat(partyInfoService.validateKeysToUrls(existingPartyInfo, newPartyInfo)).isFalse();
    }

    @Test
    public void removeRecipient() {

        PartyInfoStore anotherPartyInfoStore = mock(PartyInfoStore.class);
        ConfigService anotherConfigService = mock(ConfigService.class);

        PartyInfoService anotherPartyInfoService =
                new PartyInfoServiceImpl(anotherPartyInfoStore, anotherConfigService, enclave);

        anotherPartyInfoService.removeRecipient("http://someother.com");

        verify(anotherPartyInfoStore).removeRecipient("http://someother.com");
    }

    @Test
    public void updateWithPartyInfoRecipientUrlThatsTheSameAsCurrentNode() {

        when(configService.isDisablePeerDiscovery()).thenReturn(true);

        FeatureToggles featureToggles = mock(FeatureToggles.class);
        when(featureToggles.isEnableRemoteKeyValidation()).thenReturn(false);

        when(configService.featureToggles()).thenReturn(featureToggles);

        String url = "http://somedomain.com";

        Peer peer = mock(Peer.class);

        when(peer.getUrl()).thenReturn(url);
        when(configService.getPeers()).thenReturn(singletonList(peer));

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn(url);

        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn(url);
        when(partyInfo.getRecipients()).thenReturn(singleton(recipient));

        PartyInfo result = partyInfoService.updatePartyInfo(partyInfo);
        assertThat(result).isNotNull();

        assertThat(result.getRecipients()).hasSize(1);

        verify(configService).getPeers();
        verify(configService).featureToggles();
        verify(configService).isDisablePeerDiscovery();
    }

    @Test
    public void validateAndExtractValidRecipients() {
        PartyInfo partyInfo = mock(PartyInfo.class);
        PartyInfoValidatorCallback partyInfoValidatorCallback = mock(PartyInfoValidatorCallback.class);
        partyInfoService.validateAndExtractValidRecipients(partyInfo, partyInfoValidatorCallback);
        verify(partyInfoValidator).validateAndFetchValidRecipients(partyInfo, partyInfoValidatorCallback);
    }

    @Test
    public void unencryptSampleData() {

        byte[] data = "Hellow".getBytes();

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        PublicKey recipientKey = mock(PublicKey.class);
        when(encodedPayload.getRecipientKeys()).thenReturn(Arrays.asList(recipientKey));

        when(payloadEncoder.decode(data)).thenReturn(encodedPayload);

        partyInfoService.unencryptSampleData(data);

        verify(enclave).unencryptTransaction(encodedPayload, recipientKey);
        verify(payloadEncoder).decode(data);
    }
}
