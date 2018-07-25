package com.quorum.tessera.nacl;

import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class NonceTest {

    @Test
    public void differentClassesAreNotEqual() {
        final Object nonce = new Nonce("test".getBytes());
        final boolean isEqual = Objects.equals(nonce, "test");

        assertThat(isEqual).isFalse();
    }

    @Test
    public void sameObjectIsEqual() {
        final Nonce nonce = new Nonce(new byte[]{});

        assertThat(nonce).isEqualTo(nonce);
    }

    @Test
    public void differentObjectsWithSameBytesAreEqual() {
        final Nonce nonce1 = new Nonce(new byte[]{5, 6, 7});
        final Nonce nonce2 = new Nonce(new byte[]{5, 6, 7});

        assertThat(nonce1).isEqualTo(nonce2);
    }

    @Test
    public void toStringGivesCorrectOutput() {
        final Nonce nonce = new Nonce(new byte[]{5, 6, 7});

        final String toString = nonce.toString();

        assertThat(toString).isEqualTo("[5, 6, 7]");
    }

    @Test
    public void hashCodeIsHashcodeOfArray() {
        final byte[] nonceBytes = new byte[]{5, 6, 7};
        final Nonce nonce = new Nonce(nonceBytes);

        assertThat(nonce.hashCode()).isEqualTo(Arrays.hashCode(nonceBytes));
    }

}
