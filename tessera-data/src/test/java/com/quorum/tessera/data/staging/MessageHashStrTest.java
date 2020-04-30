package com.quorum.tessera.data.staging;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageHashStrTest {

    @Test
    public void messageHashMakesCopyOfInput() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHashStr hash = new MessageHashStr(testMessage);

        assertThat(hash).isNotEqualTo(testMessage);
        assertThat(testMessage).isEqualTo(hash.getHashBytes());
    }

    @Test
    public void differentInstancesOfSameBytesIsEqual() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHashStr hash1 = new MessageHashStr(testMessage);
        final MessageHashStr hash2 = new MessageHashStr(testMessage);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    public void differentObjectTypesAreNotEqual() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHashStr hash1 = new MessageHashStr(testMessage);

        assertThat(hash1).isNotEqualTo("test_message");
    }

    @Test
    public void sameObjectIsEqual() {
        final MessageHashStr hash = new MessageHashStr();
        hash.setHashBytes("I LOVE SPARROWS".getBytes());
        assertThat(hash).isEqualTo(hash).hasSameHashCodeAs(hash);
    }

    @Test
    public void toStringOutputsCorrectString() {

        // dmFs is "val" encoded as base64 in UTF_8

        final MessageHashStr hash = new MessageHashStr();
        hash.setHash("dmFs");

        final String toString = hash.toString();

        assertThat(toString).isEqualTo("dmFs");
    }
}
