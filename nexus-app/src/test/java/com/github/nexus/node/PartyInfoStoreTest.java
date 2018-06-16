package com.github.nexus.node;

import com.github.nexus.TestConfiguration;
import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.Key;
import com.github.nexus.node.model.Party;
import com.github.nexus.node.model.PartyInfo;
import com.github.nexus.node.model.Recipient;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class PartyInfoStoreTest {

    private Configuration configuration = new TestConfiguration();

    private PartyInfoStore partyInfoStore;

    @Before
    public void init() {

        this.partyInfoStore = new PartyInfoStore(configuration);

    }

    @Test
    public void registeringSamePublicKeyTwice() {

        final String ourUrl = this.configuration.uri().toString();

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
