package com.quorum.tessera.config;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KeyDataConfigTest {

    @Test
    public void getValue() {
        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);

        keyDataConfig.getValue();
        verify(privateKeyData).getValue();

    }

    @Test
    public void getPassword() {
        PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
        KeyDataConfig keyDataConfig = new KeyDataConfig(privateKeyData, PrivateKeyType.LOCKED);

        keyDataConfig.getPassword();
        verify(privateKeyData).getPassword();

    }

}
