package com.quorum.tessera.config.keypairs;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyPairTypeTest {

    @Test
    public void values() {
        for(KeyPairType value : KeyPairType.values()) {
            assertThat(value).isNotNull();
            assertThat(KeyPairType.valueOf(value.name())).isSameAs(value);
        }
    }

}
