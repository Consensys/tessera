package com.quorum.tessera.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionTest {

    @Test
    public void getVersion() {
        String version = Version.getVersion();
        assertThat(version).isEqualTo(MockVersion.VERSION);
    }

    @Test
    public void getDefaultVersion() {
        String version = new Version(){}.version();
        assertThat(version).isNull();
    }

}
