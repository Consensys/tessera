package com.github.nexus.config;

import java.io.InputStream;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ConfigFactoryTest {

    @Test
    public void createFactoryAndThenCreateConfig() {
        ConfigFactory configFactory = ConfigFactory.create();
        InputStream inputStream = mock(InputStream.class);
        Config config =  configFactory.create(inputStream);
        verifyZeroInteractions(inputStream,config);
        
    }

}
