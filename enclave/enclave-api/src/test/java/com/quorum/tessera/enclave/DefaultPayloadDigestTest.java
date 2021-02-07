package com.quorum.tessera.enclave;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultPayloadDigestTest {

    @Test
    public void digest() {
        PayloadDigest digest = new DefaultPayloadDigest();
        String cipherText = "cipherText";
        byte[] result = digest.digest(cipherText.getBytes());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(64);
    }

}
