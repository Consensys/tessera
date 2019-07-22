package com.quorum.tessera.admin;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicKeyResponseTest {

    @Test
    public void getter() {
        String key = "key";
        PublicKeyResponse pkr = new PublicKeyResponse(key);
        assertThat(pkr.getPublicKey()).isEqualTo(key);
    }
}
