
package com.github.nexus.config;

import java.io.InputStream;
import static org.mockito.Mockito.mock;


public class MockConfigFactory implements ConfigFactory {

    @Override
    public Config create(InputStream inputStream) {
        return mock(Config.class);
    }
    
}
