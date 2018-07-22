package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.SslTrustMode;
import java.io.IOException;
import java.io.InputStream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class TomlConfigFactoryTest {

    private TomlConfigFactory tomlConfigFactory;

    @Before
    public void onSetup() throws Exception {
        tomlConfigFactory = new TomlConfigFactory();
    }

    @Test
    public void createConfigFromSampleFile() throws IOException {
        try (InputStream configData = getClass().getResourceAsStream("/sample.conf")) {
            Config result = tomlConfigFactory.create(configData);
            assertThat(result).isNotNull();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createWithKeysNotSupported() throws IOException {
        InputStream configData = mock(InputStream.class);
        InputStream keyConfigData = mock(InputStream.class);

        tomlConfigFactory.create(configData, keyConfigData);
    }

    @Test
    public void resolveSslTrustModeForCaOrTofu() {
        SslTrustMode result = TomlConfigFactory.resolve("ca-or-tofu");
        assertThat(result).isIn(SslTrustMode.CA, SslTrustMode.TOFU);

    }

    @Test
    public void resolveSslTrustMode() {
        for (SslTrustMode mode : SslTrustMode.values()) {
            SslTrustMode result = TomlConfigFactory.resolve(mode.name().toLowerCase());
            assertThat(result).isEqualTo(mode);
        }
    }
    
    @Test
    public void resolveSslTrustModeNone() {
        
        assertThat(TomlConfigFactory.resolve(null)).isEqualTo(SslTrustMode.NONE);
        assertThat(TomlConfigFactory.resolve("BOGUS")).isEqualTo(SslTrustMode.NONE);
    }

}
