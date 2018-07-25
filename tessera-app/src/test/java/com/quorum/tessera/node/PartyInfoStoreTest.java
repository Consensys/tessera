package com.quorum.tessera.node;


import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartyInfoStoreTest {

    private String annoyingUriAsAString = "FIXME";
    
    private ServerConfig configuration;

    private PartyInfoStore partyInfoStore;

    @Before
    public void onSetUp() throws URISyntaxException {
        configuration = mock(ServerConfig.class);
        when(configuration.getServerUri()).thenReturn(new URI(annoyingUriAsAString));
        this.partyInfoStore = new PartyInfoStore(configuration);

    }

    @Test
    public void registeringSamePublicKeyTwice() {

        final String ourUrl = annoyingUriAsAString;

        final Set<Recipient> ourKeys = singleton(
            new Recipient(new Key("some-key".getBytes()), ourUrl)
        );
        final Set<Party> parties = singleton(
            new Party("http://other-node.com:8080")
        );

        final PartyInfo initialInfo = new PartyInfo(ourUrl, ourKeys, parties);

        partyInfoStore.store(initialInfo);
        partyInfoStore.store(initialInfo);

        final Set<Party> retrievedParties = partyInfoStore.getPartyInfo().getParties();
        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();
        final String fetchedUrl = partyInfoStore.getPartyInfo().getUrl();

        assertThat(retrievedParties).hasSize(1).containsExactly(new Party("http://other-node.com:8080"));
        assertThat(retrievedRecipients).hasSize(1).containsExactly(
            new Recipient(new Key("some-key".getBytes()), ourUrl)
        );

        assertThat(fetchedUrl).isEqualTo(ourUrl);
    }

}
