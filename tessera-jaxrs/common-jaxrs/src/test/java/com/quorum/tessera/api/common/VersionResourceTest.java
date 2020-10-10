package com.quorum.tessera.api.common;

import com.quorum.tessera.api.Version;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionResourceTest {

    private VersionResource instance;

    @Before
    public void onSetUp() {
        instance = new VersionResource();
    }

    @Test
    public void getVersion() {
        assertThat(instance.getVersion()).isEqualTo(Version.getVersion());
    }

    @Test
    public void getDistributionVersion() {
        assertThat(instance.getDistributionVersion()).isEqualTo(Version.getVersion());
    }

    @Test
    public void getVersions() {
        final JsonObject versions = instance.getVersions();

        final String expected = "{\"versions\":[{\"version\":\"1.0\"},{\"version\":\"2.0\"}]}";

        // since the versions should be sorted, we know that the JSON string is in a particular order
        final String versionJson = versions.toString();

        assertThat(versionJson).isEqualTo(expected);
    }
}
