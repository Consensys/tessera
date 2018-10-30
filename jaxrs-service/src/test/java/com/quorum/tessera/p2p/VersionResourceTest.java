package com.quorum.tessera.p2p;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.p2p.VersionResource;
import org.junit.Before;
import org.junit.Test;

public class VersionResourceTest {

    private VersionResource instance;

    public VersionResourceTest() {
    }

    @Before
    public void onSetUp() {
        instance = new VersionResource();
    }

    @Test
    public void getVersion() {

        assertThat(instance.getVersion())
                .isEqualTo("No version defined yet!");

    }
}
