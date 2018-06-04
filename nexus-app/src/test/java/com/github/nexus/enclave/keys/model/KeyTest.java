package com.github.nexus.enclave.keys.model;

import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyTest {

    @Test
    public void differentClassesAreNotEqual() {
        final boolean isEqual = Objects.equals(new Key("test".getBytes()), "test");

        assertThat(isEqual).isFalse();
    }

}
