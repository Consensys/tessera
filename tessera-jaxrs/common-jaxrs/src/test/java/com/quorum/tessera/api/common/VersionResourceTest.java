package com.quorum.tessera.api.common;

import org.junit.Before;
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

    @Test
    public void getVersion() {
        assertThat(instance.getVersion()).isEqualTo("v2");
    }

    @Test
    public void getVersions() {
        assertThat(instance.getVersions())
            .containsExactlyElementsOf(Stream.of("v1","v2").map(Json::createValue).collect(Collectors.toSet()));
    }
}
