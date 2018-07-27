package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class KeyDataTest {

    @Test
    public void hasKeys() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        KeyData keyData = new KeyData(keyDataConfig, "privateKey", "publicKey", null, null);

        assertThat(keyData.hasKeys()).isTrue();
    }

    @Test
    public void hasKeysNullKeys() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        KeyData keyData = new KeyData(keyDataConfig, null, null, null, null);

        assertThat(keyData.hasKeys()).isFalse();
    }

    @Test
    public void hasKeysNullPrivateKey() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        KeyData keyData = new KeyData(keyDataConfig, null, "publicKey", null, null);

        assertThat(keyData.hasKeys()).isFalse();
    }

    @Test
    public void hasKeysNullPiublicKey() {
        KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        KeyData keyData = new KeyData(keyDataConfig, "privateKey", null, null, null);

        assertThat(keyData.hasKeys()).isFalse();
    }
}
