package com.github.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PrivateKeyTest {

    @Test
    public void delegateGetters() {
        PrivateKeyData data = mock(PrivateKeyData.class);
        PrivateKey privateKey = new PrivateKey(data, null, PrivateKeyType.LOCKED);

        privateKey.getArgonOptions();
        privateKey.getAsalt();
        privateKey.getPassword();
        privateKey.getValue();
        privateKey.getSbox();
        privateKey.getSnonce();

        assertThat(privateKey.getPrivateKeyData()).isSameAs(data);

        verify(data).getArgonOptions();
        verify(data).getAsalt();
        verify(data).getPassword();
        verify(data).getValue();
        verify(data).getSbox();
        verify(data).getSnonce();

        verifyNoMoreInteractions(data);

    }

    @Test
    public void loadSample() {

    }

}
