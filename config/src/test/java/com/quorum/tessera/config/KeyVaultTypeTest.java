package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultTypeTest {

    @Test
    public void values() {
        for (KeyVaultType t : KeyVaultType.values()) {
            assertThat(t).isNotNull();
            assertThat(KeyVaultType.valueOf(t.name())).isSameAs(t);
        }
    }
}
