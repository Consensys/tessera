package com.quorum.tessera.node;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.core.config.ConfigService;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.key.exception.KeyNotFoundException;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartyInfoServiceTest {

    private static final String URI = "http://localhost:8080";

    private static final Set<Party> NEW_PARTIES = Stream.of(new Party("url1"), new Party("url2")).collect(toSet());

    private PartyInfoStore partyInfoStore;

    private Config configuration;

    private ConfigService configService;

    private KeyManager keyManager;

    private PartyInfoService partyInfoService;

    @Before
    public void onSetUp() {

        this.partyInfoStore = mock(PartyInfoStore.class);
        this.configuration = mock(Config.class);
        this.keyManager = mock(KeyManager.class);
        this.configService = mock(ConfigService.class);

        when(configService.getConfig()).thenReturn(configuration);

        final ServerConfig serverConfig = new ServerConfig("http://localhost", 8080, 50521, null, null, null, null);

        when(configuration.getServerConfig()).thenReturn(serverConfig);

        final Peer peer = new Peer("http://other-node.com:8080");
        when(configuration.getPeers()).thenReturn(singletonList(peer));

        final Set<Key> ourKeys = new HashSet<>(
                Arrays.asList(
                        new Key("some-key".getBytes()),
                        new Key("another-public-key".getBytes())
                )
        );
        doReturn(ourKeys).when(keyManager).getPublicKeys();

        this.partyInfoService = new PartyInfoServiceImpl(partyInfoStore, configService, keyManager);
    }

    @After
    public void after() {
        //Called in constructor
        verify(keyManager).getPublicKeys();
        verify(configuration).getPeers();
        verify(configuration).getServerConfig();

        verify(partyInfoStore, atLeast(1)).store(any(PartyInfo.class));

        verifyNoMoreInteractions(partyInfoStore);
        verifyNoMoreInteractions(keyManager);
        verifyNoMoreInteractions(configuration);
    }

    @Test
    public void initialPartiesCorrectlyReadFromConfiguration() {

        final PartyInfo partyInfo = new PartyInfo(URI, emptySet(), singleton(new Party("http://other-node.com:8080")));
        doReturn(partyInfo).when(partyInfoStore).getPartyInfo();

        final Set<Party> initialParties = partyInfoService.getPartyInfo().getParties();
        final Set<Recipient> initialRecipients = partyInfoService.getPartyInfo().getRecipients();
        final String ourUrl = partyInfoService.getPartyInfo().getUrl();

        assertThat(initialParties).hasSize(1).containsExactly(new Party("http://other-node.com:8080"));
        assertThat(initialRecipients).hasSize(0);
        assertThat(ourUrl).isEqualTo(URI);

        verify(partyInfoStore).store(any(PartyInfo.class));
        verify(partyInfoStore, times(3)).getPartyInfo();

        //TODO: add a captor for verification
    }

    @Test
    public void registeringPublicKeysUsesOurUrl() {

        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);

        verify(partyInfoStore).store(captor.capture());
        verify(keyManager).getPublicKeys();
        verify(configuration).getPeers();
        verify(configuration).getServerConfig();

        final List<Recipient> allRegisteredKeys = captor
                .getAllValues()
                .stream()
                .map(PartyInfo::getRecipients)
                .flatMap(Set::stream)
                .collect(toList());

        assertThat(allRegisteredKeys)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new Recipient(new Key("some-key".getBytes()), URI),
                        new Recipient(new Key("another-public-key".getBytes()), URI)
                );
    }

    @Test
    public void updatePartyInfoDelegatesToStore() {

        final String secondParty = "http://other-node.com:8080";
        final String thirdParty = "http://third-url.com:8080";

        final PartyInfo secondNodePartyInfo = new PartyInfo(secondParty, emptySet(), emptySet());
        final PartyInfo thirdNodePartyInfo = new PartyInfo(thirdParty, emptySet(), emptySet());

        partyInfoService.updatePartyInfo(secondNodePartyInfo);
        partyInfoService.updatePartyInfo(thirdNodePartyInfo);

        verify(partyInfoStore).store(secondNodePartyInfo);
        verify(partyInfoStore).store(thirdNodePartyInfo);
        verify(partyInfoStore, times(3)).store(any(PartyInfo.class));
        verify(partyInfoStore, times(2)).getPartyInfo();
        verify(configuration, times(2)).isDisablePeerDiscovery();
    }

    @Test
    public void getRecipientURLFromPartyInfoStore() {

        final Recipient recipient = new Recipient(new Key("key".getBytes()), "someurl");
        final PartyInfo partyInfo = new PartyInfo(URI, singleton(recipient), emptySet());
        doReturn(partyInfo).when(partyInfoStore).getPartyInfo();

        final String result = partyInfoService.getURLFromRecipientKey(new Key("key".getBytes()));
        assertThat(result).isEqualTo("someurl");

        verify(partyInfoStore).getPartyInfo();
    }

    @Test
    public void getRecipientURLFromPartyInfoStoreFailsIfKeyDoesntExist() {

        doReturn(new PartyInfo("", emptySet(), emptySet())).when(partyInfoStore).getPartyInfo();

        final Key failingKey = new Key("otherKey".getBytes());
        final Throwable throwable = catchThrowable(() -> partyInfoService.getURLFromRecipientKey(failingKey));
        assertThat(throwable).isInstanceOf(KeyNotFoundException.class).hasMessage("Recipient not found");

        verify(partyInfoStore).getPartyInfo();
    }

    @Test
    public void diffPartyInfoReturnsFullSetOnEmptyStore() {
        doReturn(new PartyInfo("", emptySet(), emptySet())).when(partyInfoStore).getPartyInfo();

        final PartyInfo incomingInfo = new PartyInfo("", emptySet(), NEW_PARTIES);

        final Set<Party> unsavedParties = this.partyInfoService.findUnsavedParties(incomingInfo);

        assertThat(unsavedParties)
                .hasSize(2)
                .containsExactlyInAnyOrder(NEW_PARTIES.toArray(new Party[0]));

        verify(partyInfoStore).getPartyInfo();

    }

    @Test
    public void diffPartyInfoReturnsEmptySetOnFullStore() {
        doReturn(new PartyInfo("", emptySet(), NEW_PARTIES)).when(partyInfoStore).getPartyInfo();

        final PartyInfo incomingInfo = new PartyInfo("", emptySet(), NEW_PARTIES);

        final Set<Party> unsavedParties = this.partyInfoService.findUnsavedParties(incomingInfo);

        assertThat(unsavedParties).isEmpty();

        verify(partyInfoStore).getPartyInfo();

    }

    @Test
    public void diffPartyInfoReturnsNodesNotInStore() {
        doReturn(new PartyInfo("", emptySet(), singleton(new Party("url1"))))
                .when(partyInfoStore)
                .getPartyInfo();

        final PartyInfo incomingInfo = new PartyInfo("", emptySet(), NEW_PARTIES);

        final Set<Party> unsavedParties = this.partyInfoService.findUnsavedParties(incomingInfo);

        assertThat(unsavedParties)
                .hasSize(1)
                .containsExactlyInAnyOrder(new Party("url2"));

        verify(partyInfoStore).getPartyInfo();

    }

    @Test
    public void updatePartyInfoDelegatesToStoreAutoDiscoveryDisabled() {

        when(configuration.isDisablePeerDiscovery()).thenReturn(true);

        Set<Party> parties = Stream.of("MyURI", "MyOtherUri")
                .map(Party::new).collect(Collectors.toSet());

        PartyInfo partyInfo = new PartyInfo("MyURI", EMPTY_SET, parties);

        when(partyInfoStore.getPartyInfo()).thenReturn(partyInfo);

        PartyInfo forUpdate = new PartyInfo("UnknownUri", EMPTY_SET, parties);
        try {
            partyInfoService.updatePartyInfo(forUpdate);
            failBecauseExceptionWasNotThrown(AutoDiscoveryDisabledException.class);

        } catch (AutoDiscoveryDisabledException ex) {
            verify(configuration).isDisablePeerDiscovery();
            verify(partyInfoStore).getPartyInfo();
        }
    }

    @Test
    public void updatePartyInfoDelegatesToStoreAutoDiscoveryDisabledDifferentParties() {

        when(configuration.isDisablePeerDiscovery()).thenReturn(true);

        Set<Party> parties = Stream.of("MyURI", "MyOtherUri")
                .map(Party::new).collect(Collectors.toSet());

        PartyInfo partyInfo = new PartyInfo("MyURI", EMPTY_SET, parties);

        when(partyInfoStore.getPartyInfo()).thenReturn(partyInfo);

        Set<Party> otherParties = Stream.of("OtherURI", "OtherUri")
                .map(Party::new).collect(Collectors.toSet());
        
        PartyInfo forUpdate = new PartyInfo("MyOtherUri", EMPTY_SET, otherParties);
        try {
            partyInfoService.updatePartyInfo(forUpdate);
            failBecauseExceptionWasNotThrown(AutoDiscoveryDisabledException.class);

        } catch (AutoDiscoveryDisabledException ex) {
            verify(configuration).isDisablePeerDiscovery();
            verify(partyInfoStore).getPartyInfo();
        }

    }

}
