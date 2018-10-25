package com.quorum.tessera.node;


import com.quorum.tessera.core.config.ConfigService;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartyInfoStoreTest {

    private String uri = "http://localhost:8080";

    private ConfigService configService;

    private PartyInfoStore partyInfoStore;

    @Before
    public void onSetUp() throws URISyntaxException {
        this.configService = mock(ConfigService.class);
        when(configService.getServerUri()).thenReturn(new URI(uri));

        this.partyInfoStore = new PartyInfoStore(configService);

    }

    @Test
    public void registeringSamePublicKeyTwice() {

        final PublicKey testKey = PublicKey.from("some-key".getBytes());

        final Set<Recipient> ourKeys = singleton(new Recipient(testKey, uri));
        final Set<Party> parties = singleton(new Party("http://other-node.com:8080"));

        final PartyInfo initialInfo = new PartyInfo(uri, ourKeys, parties);

        partyInfoStore.store(initialInfo);
        partyInfoStore.store(initialInfo);

        final Set<Party> retrievedParties = partyInfoStore.getPartyInfo().getParties();
        final Set<Recipient> retrievedRecipients = partyInfoStore.getPartyInfo().getRecipients();
        final String fetchedUrl = partyInfoStore.getPartyInfo().getUrl();

        assertThat(fetchedUrl).isEqualTo(uri + "/");
        assertThat(retrievedParties)
            .hasSize(2)
            .containsExactlyInAnyOrder(new Party("http://other-node.com:8080"), new Party(uri + "/"));
        assertThat(retrievedRecipients)
            .hasSize(1)
            .containsExactly(new Recipient(testKey, uri));
    }

}
