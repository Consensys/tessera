package com.github.nexus.configuration.model;

import org.junit.Test;

import javax.json.Json;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyDataTest {

    private final KeyData keyData = new KeyData("pub", Json.createObjectBuilder().build(), "password");

    @Test
    public void differentClassesAreNotEqual() {
        final boolean isEqual = Objects.equals(keyData, "test");

        assertThat(isEqual).isFalse();
    }

    @Test
    public void sameInstanceIsEqual() {
        assertThat(keyData).isEqualTo(keyData).isSameAs(keyData);
    }

    @Test
    public void hashCodeTest() {
        assertThat(keyData)
            .hasSameHashCodeAs(new KeyData("pub", Json.createObjectBuilder().build(), "password"));

    }

    @Test
    public void twoDifferentObjectsAreEqualWithSameData() {
        assertThat(keyData)
            .isEqualTo(new KeyData("pub", Json.createObjectBuilder().build(), "password"));
    }

    @Test
    public void twoDifferentObjectsAreNotEqualWithDifferentData() {
        assertThat(keyData)
            .isNotEqualTo(new KeyData("pub2", Json.createObjectBuilder().build(), "password"));
    }

    @Test
    public void getters() {
        assertThat(keyData.getPublicKey()).isEqualTo("pub");
        assertThat(keyData.getPassword()).isEqualTo("password");
        assertThat(keyData.getPrivateKey().toString()).isEqualTo("{}");
    }

}
