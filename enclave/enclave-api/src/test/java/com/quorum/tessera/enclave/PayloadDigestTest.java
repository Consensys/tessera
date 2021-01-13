package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PayloadDigestTest {

    @Test
    public void defaultDigest() {
        PayloadDigest digest = new PayloadDigest.Default();
        String cipherText = "cipherText";
        byte[] result = digest.digest(cipherText.getBytes());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(64);
    }

    @Test
    public void digest32Bytes() {
        PayloadDigest digest = new PayloadDigest.SHA512256();
        String cipherText = "cipherText";
        byte[] result = digest.digest(cipherText.getBytes());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(32);
    }

    @Test
    public void create() {
        Config config = mock(Config.class);
        assertThat(PayloadDigest.create(config)).isNotNull().isInstanceOf(PayloadDigest.Default.class);

        when(config.isBesu()).thenReturn(true);
        assertThat(PayloadDigest.create(config)).isNotNull().isInstanceOf(PayloadDigest.SHA512256.class);
    }
}
