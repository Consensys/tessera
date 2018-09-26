
package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Config;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;


public class ConfigServiceTest {
    
    private ConfigService configService;
    
    private Config config;
    
    @Before
    public void onSetUp() {
        config = mock(Config.class);
        configService = new ConfigServiceImpl(config);
    }
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(config);
    }
    
    @Test
    public void getConfig() {
        assertThat(configService.getConfig()).isSameAs(config);
    }
    
}
