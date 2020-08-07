package com.quorum.tessera.partyinfo.model;

import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeInfoTest {

    @Test
    public void from() {

        PartyInfo partyInfo = mock(PartyInfo.class);
        VersionInfo versionInfo = mock(VersionInfo.class);

        when(partyInfo.getUrl()).thenReturn("someurl");
        when(versionInfo.supportedApiVersions()).thenReturn(Set.of("v1","v3"));

        NodeInfo nodeInfo = NodeInfo.from(partyInfo,Set.of("v1","v3"));

        assertThat(nodeInfo).isNotNull();

        assertThat(nodeInfo.getUrl()).isEqualTo("someurl");
        assertThat(nodeInfo.supportedApiVersions()).isEqualTo(Set.of("v3","v1"));

    }


    @Test(expected = NullPointerException.class)
    public void createEmptyRequiresUrl() {
        NodeInfo.Builder.create().build();
    }

    @Test
    public void createWithOnlyUrl() {
        String url = "someurl";
        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl(url)
            .build();

        assertThat(nodeInfo.getUrl()).isEqualTo(url);
        assertThat(nodeInfo.getParties()).isEmpty();
        assertThat(nodeInfo.getRecipients()).isEmpty();
        assertThat(nodeInfo.supportedApiVersions()).isEmpty();

    }

    @Test
    public void createWithEverything() {
        String url = "someurl";

        Collection<Party> parties = List.of(mock(Party.class));
        Collection<Recipient> recipients = List.of(mock(Recipient.class));
        Collection<String> supportedVersions = List.of("ONE","TWO");

        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl(url)
            .withRecipients(recipients)
            .withParties(parties)
            .withSupportedApiVersions(supportedVersions)
            .build();

        assertThat(nodeInfo.getUrl()).isEqualTo(url);
        assertThat(nodeInfo.getParties()).isEqualTo(Set.copyOf(parties));
        assertThat(nodeInfo.getRecipients()).isEqualTo(Set.copyOf(recipients));
        assertThat(nodeInfo.supportedApiVersions()).isEqualTo(Set.copyOf(supportedVersions));

    }
}
