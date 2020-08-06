package com.quorum.tessera.partyinfo.model;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeInfoTest {

    @Test
    public void create() {

        PartyInfo partyInfo = mock(PartyInfo.class);
        VersionInfo versionInfo = mock(VersionInfo.class);

        when(partyInfo.getUrl()).thenReturn("someurl");
        when(versionInfo.supportedApiVersions()).thenReturn(Set.of("v1","v3"));

        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .from(partyInfo)
            .withVersionInfo(versionInfo)
            .build();

        assertThat(nodeInfo).isNotNull();

        assertThat(nodeInfo.partyInfo().getUrl()).isEqualTo("someurl");
        assertThat(nodeInfo.versionInfo().supportedApiVersions()).isEqualTo(Set.of("v3","v1"));

    }
}
