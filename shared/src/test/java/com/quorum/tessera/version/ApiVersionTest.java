package com.quorum.tessera.version;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiVersionTest {

    @Test
    public void create() {
        List<String> result = ApiVersion.versions();
        assertThat(result).containsExactly("0.1","1.0","v1");

    }

}
