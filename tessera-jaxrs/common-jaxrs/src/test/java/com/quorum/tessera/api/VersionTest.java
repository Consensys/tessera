package com.quorum.tessera.api;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionTest {

    @Ignore
    @Test
    public void getVersion() {
        String version = Version.getVersion();
        assertThat(version).isEqualTo(MockVersion.VERSION);
    }


}
