package com.quorum.tessera.api.common;

import com.quorum.tessera.api.MockVersion;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.json.Json;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionResourceTest {

    private VersionResource instance;

    public VersionResourceTest() {}

    @Before
    public void onSetUp() {
        instance = new VersionResource();
    }

    @Ignore
    @Test
    public void getVersion() {
        assertThat(instance.getVersion()).isEqualTo(MockVersion.VERSION);
    }

    @Ignore
    @Test
    public void getDistributionVersion() {
        assertThat(instance.getDistributionVersion()).isEqualTo(MockVersion.VERSION);
    }


    @Test
    public void getVersions() {
        assertThat(instance.getVersions())
            .containsExactlyElementsOf(Stream.of("1.0", "2.0")
                .map(Json::createValue)
                .collect(Collectors.toSet()));
    }
}
