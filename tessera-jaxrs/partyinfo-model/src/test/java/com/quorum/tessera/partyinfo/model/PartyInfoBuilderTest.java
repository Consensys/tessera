package com.quorum.tessera.partyinfo.model;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PartyInfoBuilderTest {

    private PartyInfoBuilder partyInfoBuilder;

    @Before
    public void beforeTest() {
        partyInfoBuilder = PartyInfoBuilder.create();
    }

    @Test
    public void buildOnlyWithUrl() {

        final String url = "http://pinfo.com";

        PartyInfo partyInfo = partyInfoBuilder.withUri(url).build();

        assertThat(partyInfo).isNotNull();
        assertThat(partyInfo.getUrl()).isEqualTo(url.concat("/"));
        assertThat(partyInfo.getRecipients()).isEmpty();
        assertThat(partyInfo.getParties()).isEmpty();
    }

    @Test
    public void buildWithEverything() {

        final String url = "http://pinfo.com";
        PublicKey publicKey = mock(PublicKey.class);
        PartyInfo partyInfo =
                partyInfoBuilder.withUri(url).withRecipients(Map.of(publicKey, "http://bobbysixkiller.com")).build();

        assertThat(partyInfo).isNotNull();
        assertThat(partyInfo.getUrl()).isEqualTo(url.concat("/"));
        assertThat(partyInfo.getRecipients()).hasSize(1);
        assertThat(partyInfo.getParties()).hasSize(1);

        Recipient recipient = partyInfo.getRecipients().iterator().next();
        assertThat(recipient.getKey()).isSameAs(publicKey);
        assertThat(recipient.getUrl()).isEqualTo("http://bobbysixkiller.com/");

        Party party = partyInfo.getParties().iterator().next();
        assertThat(party.getUrl()).isEqualTo("http://bobbysixkiller.com/");
    }
}
