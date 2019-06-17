package com.quorum.tessera.admin;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.ConfigFileStore;
import com.quorum.tessera.enclave.Enclave;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConfigServiceTest {

    private ConfigService configService;

    private Config config;

    private ConfigFileStore configFileStore;

    private Enclave enclave;

    @Before
    public void onSetUp() {
        config = mock(Config.class);
        configFileStore = mock(ConfigFileStore.class);
        enclave = mock(Enclave.class);
        configService = new ConfigServiceImpl(config, enclave, configFileStore);
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
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        URI result = configService.getServerUri();
        assertThat(result).isSameAs(serverUri);


        verify(config).getP2PServerConfig();
        verify(serverConfig).getServerUri();
    }

    @Test
    public void getPublicKeys() {
        configService.getPublicKeys();
        verify(enclave).getPublicKeys();
    }

    @Test
    public void getLedgerId() {

        ServerConfig serverConfig = mock(ServerConfig.class);
        String ledgerId = "ledger";
        when(serverConfig.getLedgerId()).thenReturn(ledgerId);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);
        String result = configService.getLedgerId();
        assertThat(result).isEqualTo(ledgerId);

        verify(config).getP2PServerConfig();
        verify(serverConfig).getLedgerId();
    }
}
