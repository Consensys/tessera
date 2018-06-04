package com.github.nexus.enclave.keys.model;

import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyTest {

    @Test
    public void different_classes_are_not_equal() {
        final boolean isEqual = Objects.equals(new Key("test".getBytes()), "test");

        assertThat(isEqual).isFalse();
    }

}
