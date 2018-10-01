package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.ConfigFileStore;
import java.net.URI;
import java.net.URISyntaxException;
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
        configService = new ConfigServiceImpl(config, configFileStore);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(config, configFileStore);
    }

    @Test
    public void isUseWhileList() {
        when(config.isUseWhiteList()).thenReturn(false);

        assertThat(configService.isUseWhiteList()).isFalse();

        when(config.isUseWhiteList()).thenReturn(true);
        assertThat(configService.isUseWhiteList()).isTrue();

        verify(config, times(2)).isUseWhiteList();
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

    @Test
    public void isDisablePeerDiscovery() {
        when(config.isDisablePeerDiscovery()).thenReturn(false);

        assertThat(configService.isDisablePeerDiscovery()).isFalse();

        when(config.isDisablePeerDiscovery()).thenReturn(true);
        assertThat(configService.isDisablePeerDiscovery()).isTrue();

        verify(config, times(2)).isDisablePeerDiscovery();
    }
    
    @Test
    public void getServerUri() throws URISyntaxException {
        ServerConfig serverConfig = mock(ServerConfig.class);
        URI serverUri = new URI("someuri");
        when(serverConfig.getServerUri()).thenReturn(serverUri);
        
        when(config.getServerConfig()).thenReturn(serverConfig);
        
        URI result = configService.getServerUri();
        
        assertThat(result).isSameAs(serverUri);
        
        verify(config).getServerConfig();
        verify(serverConfig).getServerUri();
        
    }

}
