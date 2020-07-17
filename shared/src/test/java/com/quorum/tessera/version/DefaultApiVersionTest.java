package com.quorum.tessera.version;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultApiVersionTest {

    private DefaultApiVersion apiVersion = new DefaultApiVersion();

    @Test
    public void getVersion() {
         assertThat(apiVersion.getVersion()).isNull();
    }

}
