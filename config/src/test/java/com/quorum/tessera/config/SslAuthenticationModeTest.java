package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SslAuthenticationModeTest {

    @Test
    public void testValues() {
        for (SslAuthenticationMode t : SslAuthenticationMode.values()) {
            assertThat(t).isNotNull();
            assertThat(SslAuthenticationMode.valueOf(t.name())).isSameAs(t);
        }
    }
}
