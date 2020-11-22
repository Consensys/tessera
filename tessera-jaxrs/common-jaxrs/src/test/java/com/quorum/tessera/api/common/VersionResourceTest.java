package com.quorum.tessera.api.common;

import com.quorum.tessera.api.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class VersionResourceTest {

    private VersionResource instance;

    private Version expectedVersion;

    private static final String VERSION_VALUE = "MOCK";

    @Before
    public void onSetUp() {
        expectedVersion = mock(Version.class);
        when(expectedVersion.version()).thenReturn(VERSION_VALUE);
        instance = new VersionResource(expectedVersion);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(expectedVersion);
    }

    @Test
    public void getVersion() {
        assertThat(instance.getVersion())
            .isEqualTo(VERSION_VALUE);
        verify(expectedVersion).version();
    }

    @Test
    public void getDistributionVersion() {
        assertThat(instance.getDistributionVersion())
            .isEqualTo(VERSION_VALUE);

        verify(expectedVersion).version();
    }

    @Test
    public void getVersions() {
        assertThat(instance.getVersions())
            .containsExactlyElementsOf(Stream.of("1.0", "2.0")
                .map(Json::createValue)
                .collect(Collectors.toSet()));
    }
}
