package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ConfigTest {

  @Test
  public void createDefault() {
    Config config = new Config();
    assertThat(config).isNotNull();
  }

  @Test
  public void createWithNullArgs() {
    Config config = new Config(null, null, null, null, null, null, false, false);
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
    config.setServerConfigs(Arrays.asList(serverConfig));

    assertThat(config.getP2PServerConfig()).isSameAs(serverConfig);
  }

  @Test
  public void getP2PServerConfigSingleServerByWrongAppType() {
    Config config = new Config();
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setApp(AppType.THIRD_PARTY);
    config.setServerConfigs(Arrays.asList(serverConfig));

    assertThat(config.getP2PServerConfig()).isNull();
  }

  @Test
  public void areServerConfigsNull() {
    Config config = new Config();
    Path unixServerPath = mock(Path.class);
    config.setUnixSocketFile(unixServerPath);

    assertThat(config.getServerConfigs()).isEmpty();
    assertThat(config.isServerConfigsNull()).isTrue();

    config.setServerConfigs(Collections.EMPTY_LIST);
    assertThat(config.isServerConfigsNull()).isFalse();
  }

  // TODO: Ensure that version read from jar file works
  @Test
  public void version() {
    Config config = new Config();
    assertThat(config.getVersion()).isSameAs(Version.getVersion());
  }
}
