package com.github.nexus.config;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class LegacyPrivateKeyFileAdapterTest {

    private LegacyPrivateKeyFileAdapter adapter = new LegacyPrivateKeyFileAdapter();

    @Test
    public void marshalThrowsError() {

        final Throwable throwable = catchThrowable(() ->adapter.marshal(null));

        assertThat(throwable)
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Cannot marshal to file");

    }

    @Test
    public void unlockedKeyIsUnmarshalled() throws URISyntaxException, IOException {

        final String path = ClassLoader.getSystemResource("unlockedprivatekey.json").toURI().getPath();

        final LegacyPrivateKeyFile result = adapter.unmarshal(path);

        assertThat(result.getType()).isEqualTo(PrivateKeyType.UNLOCKED);
        assertThat(result.getBytes()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");

    }

    @Test
    public void lockedKeyIsUnmarshalled() throws URISyntaxException, IOException {

        final String path = ClassLoader.getSystemResource("lockedprivatekey.json").toURI().getPath();

        final LegacyPrivateKeyFile result = adapter.unmarshal(path);

        assertThat(result.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(result.getBytes()).isNull();
        assertThat(result.getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(result.getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");
        assertThat(result.getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(result.getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(result.getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(result.getArgonOptions().getMemory()).isEqualTo(1048576);
        assertThat(result.getArgonOptions().getParallelism()).isEqualTo(4);


    }

}
