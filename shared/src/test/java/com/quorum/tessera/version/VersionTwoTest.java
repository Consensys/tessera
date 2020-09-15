package com.quorum.tessera.version;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionTwoTest {

    private EnhancedPrivacyVersion version = new EnhancedPrivacyVersion();

    @Test
    public void getVersion() {
        assertThat(version.getVersion()).isEqualTo("v2");
    }
}
