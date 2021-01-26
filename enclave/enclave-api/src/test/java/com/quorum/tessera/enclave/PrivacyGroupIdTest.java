package com.quorum.tessera.enclave;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupIdTest {

    @Test
    public void create() {
        PrivacyGroupId id = PrivacyGroupId.from("foo".getBytes());
        assertThat(id).isNotNull();
        assertThat(id.getBytes()).isEqualTo("foo".getBytes());
        assertThat(id.getBase64()).isEqualTo("Zm9v");
    }

    @Test
    public void createFromBase64Str() {
        PrivacyGroupId id = PrivacyGroupId.from("Zm9v");
        assertThat(id).isNotNull();
        assertThat(id.getBytes()).isEqualTo("foo".getBytes());
        assertThat(id.getBase64()).isEqualTo("Zm9v");
    }

    @Test
    public void testEquals() {
        PrivacyGroupId id = PrivacyGroupId.from("foo".getBytes());
        PrivacyGroupId same = PrivacyGroupId.from("Zm9v");
        assertThat(id).isEqualTo(same);

        PrivacyGroupId bogus = PrivacyGroupId.from("another".getBytes());
        assertThat(id).isNotEqualTo(bogus);

        assertThat(id.toString()).isEqualTo(PrivacyGroupId.class.getSimpleName() + "[Zm9v]");

        assertThat(id.hashCode()).isEqualTo(Arrays.hashCode("foo".getBytes()));
    }
}
