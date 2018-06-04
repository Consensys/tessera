package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageHashTest {

    @Test
    public void message_hash_makes_copy_of_input() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHash hash = new MessageHash(testMessage);

        assertThat(hash).isNotEqualTo(testMessage);
        assertThat(Arrays.equals(testMessage, hash.getHashBytes())).isTrue();

    }

    @Test
    public void test_different_instances_of_same_bytes_is_equal() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHash hash1 = new MessageHash(testMessage);
        final MessageHash hash2 = new MessageHash(testMessage);

        assertThat(hash1).isEqualTo(hash2);

    }

    @Test
    public void test_different_object_types() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHash hash1 = new MessageHash(testMessage);

        assertThat(hash1).isNotEqualTo("test_message");

    }


}
