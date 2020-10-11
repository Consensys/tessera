package com.quorum.tessera.api.common;

import com.quorum.tessera.api.MockVersion;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
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
        assertThat(instance.getVersion()).isEqualTo("2.0");
    }

    @Test
    public void getInfo() {
        JsonObject info = instance.getInfo();

        assertThat(info).isNotNull();
        assertThat(info.getString("dist")).isEqualTo(MockVersion.VERSION);
        assertThat(info.getJsonArray("versions"))
            .containsExactlyElementsOf(Stream.of("1.0", "2.0")
                .map(Json::createValue).collect(Collectors.toSet()));
    }

    @Test
    public void getVersions() {
        assertThat(instance.getVersions())
            .containsExactlyElementsOf(Stream.of("1.0", "2.0")
                .map(Json::createValue)
                .collect(Collectors.toSet()));
    }
}
