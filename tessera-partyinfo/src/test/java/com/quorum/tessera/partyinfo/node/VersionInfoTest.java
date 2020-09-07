package com.quorum.tessera.partyinfo.node;


import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionInfoTest {

    @Test
    public void create() {
        Set<String> versions = Set.of("v1","v2");
        VersionInfo versionInfo = VersionInfo.from(versions);
        assertThat(versionInfo).isNotNull();
        assertThat(versionInfo.supportedApiVersions()).isEqualTo(versions);
    }
}
