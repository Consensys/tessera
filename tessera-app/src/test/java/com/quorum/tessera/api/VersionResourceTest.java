package com.quorum.tessera.api;

import static org.assertj.core.api.Assertions.assertThat;
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
