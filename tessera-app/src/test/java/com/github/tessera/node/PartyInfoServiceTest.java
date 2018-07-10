package com.github.tessera.node;


import com.github.tessera.config.Config;
import com.github.tessera.config.Peer;
import com.github.tessera.config.ServerConfig;
import com.github.tessera.key.KeyManager;
import com.github.tessera.nacl.Key;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import com.github.tessera.node.model.Recipient;
import java.net.URI;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static org.mockito.Mockito.*;

public class PartyInfoServiceTest {

    private PartyInfoStore partyInfoStore;

    private Config configuration;

    private PartyInfoService partyInfoService;

    private KeyManager keyManager;

    private static final String uri = "http://localhost:8080";

    @Before
    public void onSetUp() throws Exception {
        this.partyInfoStore = mock(PartyInfoStore.class);
        this.configuration = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getServerUri()).thenReturn(new URI(uri));
        when(configuration.getServerConfig()).thenReturn(serverConfig);
        
        Peer peer = mock(Peer.class);
        when(peer.getUrl()).thenReturn("http://other-node.com:8080");
        when(configuration.getPeers()).thenReturn(Arrays.asList(peer));

        this.keyManager = mock(KeyManager.class);
        this.partyInfoService = new PartyInfoServiceImpl(partyInfoStore, configuration, keyManager);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(partyInfoStore);
    }

    @Test
    public void initialPartiesCorrectlyReadFromConfiguration() {

        final PartyInfo partyInfo = new PartyInfo(uri, emptySet(), singleton(new Party("http://other-node.com:8080")));
        doReturn(partyInfo).when(partyInfoStore).getPartyInfo();

        final Set<Party> initialParties = partyInfoService.getPartyInfo().getParties();
        final Set<Recipient> initialRecipients = partyInfoService.getPartyInfo().getRecipients();
        final String ourUrl = partyInfoService.getPartyInfo().getUrl();

        assertThat(initialParties).hasSize(1).containsExactly(new Party("http://other-node.com:8080"));
        assertThat(initialRecipients).hasSize(0);
        assertThat(ourUrl).isEqualTo(uri);

        verify(partyInfoStore, times(2)).store(any(PartyInfo.class));
        verify(partyInfoStore, times(3)).getPartyInfo();

        //TODO: add a captor for verification
    }

    @Test
    public void registeringPublicKeysUsesOurUrl() {

        final String ourUrl = this.configuration.getServerConfig().getServerUri().toString();

        final Set<Key> ourPublicKeys2 = new HashSet<>();
        ourPublicKeys2.add(new Key("some-key".getBytes()));
        ourPublicKeys2.add(new Key("another-public-key".getBytes()));

        final PartyInfo partyInfo = new PartyInfo(
            uri,
            Stream.of(
                new Recipient(new Key("some-key".getBytes()), uri),
                new Recipient(new Key("another-public-key".getBytes()), uri)
            ).collect(toSet()),
            emptySet()
        );
        doReturn(partyInfo).when(partyInfoStore).getPartyInfo();


        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);
        partyInfoService.registerPublicKeys(ourUrl, ourPublicKeys2);

        final String fetchedUrl = partyInfoService.getPartyInfo().getUrl();
        assertThat(fetchedUrl).isEqualTo(ourUrl);
        verify(partyInfoStore).getPartyInfo();

        verify(partyInfoStore, times(3)).store(captor.capture());
        final List<Recipient> allRegisteredKeys = captor.getAllValues()
            .stream()
            .map(PartyInfo::getRecipients)
            .flatMap(Set::stream)
            .collect(toList());

        assertThat(allRegisteredKeys).hasSize(2).containsExactlyInAnyOrder(
            new Recipient(new Key("some-key".getBytes()), ourUrl),
            new Recipient(new Key("another-public-key".getBytes()), ourUrl)
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
        verify(partyInfoStore, times(4)).store(any(PartyInfo.class));

        verify(partyInfoStore, times(2)).getPartyInfo();

    }

    @Test
    public void getRecipientURLFromPartyInfoStore(){
        verify(partyInfoStore, times(2)).store(any());
        Recipient recipient = new Recipient(new Key("key".getBytes()),"someurl");
        PartyInfo partyInfo = new PartyInfo(uri, Collections.singleton(recipient), emptySet());
        when(partyInfoStore.getPartyInfo()).thenReturn(partyInfo);

        assertThat(partyInfoService.getURLFromRecipientKey(new Key("key".getBytes()))).isEqualTo("someurl");

        verify(partyInfoStore, times(1)).getPartyInfo();

        try {
            partyInfoService.getURLFromRecipientKey(new Key("otherKey".getBytes()));
            assertThatExceptionOfType(RuntimeException.class);
        }
        catch (Exception ex){

        }

        verify(partyInfoStore, times(2)).getPartyInfo();

    }

}
