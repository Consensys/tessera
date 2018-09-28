
package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.util.ConfigFileStore;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;


public class ConfigServiceTest {
    
    private ConfigService configService;
    
    private Config config;
    
    private ConfigFileStore configFileStore;
    
    @Before
    public void onSetUp() {
        config = mock(Config.class);
        configFileStore = mock(ConfigFileStore.class);
        configService = new ConfigServiceImpl(config,configFileStore);
    }
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(config,configFileStore);
    }
    
    @Test
    public void getConfig() {
        assertThat(configService.getConfig()).isSameAs(config);
    }
    
    @Test
    public void addPeer() {
        configService.addPeer("JUNIT");
        verify(config).addPeer(new Peer("JUNIT"));
        verify(configFileStore).save(config);
        
    }
    
    @Test
    public void getPeers() {
        configService.getPeers();
        verify(config).getPeers();
    }
    
}
