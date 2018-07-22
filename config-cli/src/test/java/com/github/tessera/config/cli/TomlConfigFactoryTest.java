package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
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
        try (InputStream configData = Main.class.getResourceAsStream("/sample.conf")) {
            Config result = tomlConfigFactory.create(configData);
            assertThat(result).isNotNull();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createWithKeysNotSupported() throws IOException {
        InputStream configData = mock(InputStream.class);
        InputStream keyConfigData = mock(InputStream.class);
        
        tomlConfigFactory.create(configData,keyConfigData);
    }

}
