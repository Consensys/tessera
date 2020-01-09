package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivateKeyTypeTest {

    @Test
    public void testValues() {
        for (PrivateKeyType t : PrivateKeyType.values()) {
            assertThat(t).isNotNull();
            assertThat(PrivateKeyType.valueOf(t.name())).isSameAs(t);
        }
    }
}
