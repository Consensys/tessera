package com.github.nexus.enclave.keys.model;

import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyPairTest {

    @Test
    public void differentClassesAreNotEqual() {
        final KeyPair keyPair = new KeyPair(
                new Key("test".getBytes()),
                new Key("test".getBytes())
        );

        final boolean isEqual = Objects.equals(keyPair, "test");

        assertThat(isEqual).isFalse();
    }

}
