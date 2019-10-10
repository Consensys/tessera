package com.quorum.tessera.api.common;

import com.quorum.tessera.config.Version;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionResourceTest {

    private VersionResource instance;

    public VersionResourceTest() {}

    @Before
    public void onSetUp() {
        instance = new VersionResource();
    }

    @Test
    public void getVersion() {
        assertThat(instance.getVersion()).isEqualTo(Version.getVersion());
    }
}
