package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyDataConfigTest {

    @Test
    public void getPassword() {
        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

        String expected = "password";

        when(privateKeyData.getPassword()).thenReturn(expected);

        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);

        assertThat(keyDataConfig.getPassword()).isEqualTo(expected);
    }

    @Test
    public void getPasswordReturnsNullIfNoPassword() {
        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

        when(privateKeyData.getPassword()).thenReturn(null);

        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);

        assertThat(keyDataConfig.getPassword()).isEqualTo(null);
    }



}
