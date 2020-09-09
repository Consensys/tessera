package com.quorum.tessera.version;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseVersionTest {

    private BaseVersion apiVersion = new BaseVersion();

    @Test
    public void getVersion() {
         assertThat(apiVersion.getVersion()).isEqualTo("v1");
    }

}
