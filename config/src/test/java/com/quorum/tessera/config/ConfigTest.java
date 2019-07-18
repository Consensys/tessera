package com.quorum.tessera.config;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {

    @Test
    public void createDefault() {
        Config config = new Config();
        assertThat(config).isNotNull();
    }

    @Test
    public void createWithNullArgs() {
        Config config = new Config(null, null, null, null, null, false, false);
        assertThat(config).isNotNull();
    }

    @Test
    public void addPeer() {

        Config config = new Config();
        assertThat(config.getPeers()).isNull();

        Peer peer = new Peer("Junit");
        config.addPeer(peer);
        assertThat(config.getPeers()).containsOnly(peer);

        Peer anotherPeer = new Peer("anotherPeer");
        config.addPeer(anotherPeer);
        assertThat(config.getPeers()).containsOnly(peer, anotherPeer);
    }

    @Test
    public void getP2PServerConfigNoServers() {
        Config config = new Config();

        assertThat(config.getP2PServerConfig()).isNull();
    }

    @Test
    public void getP2PServerConfigSingleServer() {
        Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setEnabled(true);
        config.setServerConfigs(Arrays.asList(serverConfig));

        assertThat(config.getP2PServerConfig()).isSameAs(serverConfig);
    }

    @Test
    public void getP2PServerConfigSingleServerByWrongAppType() {
        Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.THIRD_PARTY);
        serverConfig.setEnabled(true);
        config.setServerConfigs(Arrays.asList(serverConfig));

        assertThat(config.getP2PServerConfig()).isNull();
    }

    @Test
    public void getVersion() {
        Config config = new Config();
        assertThat(config.getVersion()).isNotEmpty();
    }
}
