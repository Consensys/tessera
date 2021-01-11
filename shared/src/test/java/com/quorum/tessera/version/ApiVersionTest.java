package com.quorum.tessera.version;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiVersionTest {

    @Test
    public void create() {
        assertThat(ApiVersion.versions()).containsExactlyInAnyOrder("0.1", "1.0", "v1", "v2", "2.1","3");
    }
}
