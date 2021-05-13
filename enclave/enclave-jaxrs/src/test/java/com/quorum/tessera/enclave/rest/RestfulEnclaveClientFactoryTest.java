package com.quorum.tessera.enclave.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.EnclaveClient;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class RestfulEnclaveClientFactoryTest {

  private RestfulEnclaveClientFactory restfulEnclaveClientFactory =
      new RestfulEnclaveClientFactory();

  @Test
  public void create() {
    final Config config = new Config();
    final ServerConfig serverConfig = new ServerConfig();
    serverConfig.setApp(AppType.ENCLAVE);
    serverConfig.setCommunicationType(CommunicationType.REST);

    serverConfig.setServerAddress("http://bogushost:99");
    List<ServerConfig> serverConfigs = Arrays.asList(serverConfig);
    config.setServerConfigs(serverConfigs);

    EnclaveClient result = restfulEnclaveClientFactory.create(config);

    assertThat(result).isNotNull();
  }

  @Test(expected = java.util.NoSuchElementException.class)
  public void createWithNoCommunicationTypeDefined() {
    final Config config = new Config();
    final ServerConfig serverConfig = new ServerConfig();
    serverConfig.setApp(AppType.ENCLAVE);

    serverConfig.setServerAddress("http://bogushost:99");
    List<ServerConfig> serverConfigs = Arrays.asList(serverConfig);
    config.setServerConfigs(serverConfigs);

    restfulEnclaveClientFactory.create(config);
  }

  @Test(expected = java.util.NoSuchElementException.class)
  public void createWithNoAppTypeDefined() {
    final Config config = new Config();
    final ServerConfig serverConfig = new ServerConfig();
    serverConfig.setCommunicationType(CommunicationType.REST);

    serverConfig.setServerAddress("http://bogushost:99");
    List<ServerConfig> serverConfigs = Arrays.asList(serverConfig);
    config.setServerConfigs(serverConfigs);

    restfulEnclaveClientFactory.create(config);
  }
}
