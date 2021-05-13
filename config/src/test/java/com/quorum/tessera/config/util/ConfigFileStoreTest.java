package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class ConfigFileStoreTest {

  private ConfigFileStore configFileStore;

  private Path path;

  @Before
  public void onSetUp() throws Exception {
    this.path = Files.createTempFile(UUID.randomUUID().toString(), ".junit");

    final URL sampleConfig = getClass().getResource("/sample.json");
    try (InputStream in = sampleConfig.openStream()) {
      Config initialConfig = JaxbUtil.unmarshal(in, Config.class);
      JaxbUtil.marshalWithNoValidation(initialConfig, Files.newOutputStream(path));
    }

    configFileStore = ConfigFileStore.create(path);
  }

  @Test
  public void getReturnsSameInstance() {
    assertThat(ConfigFileStore.get()).isSameAs(configFileStore);
  }

  @Test
  public void save() throws IOException {

    Config config = new Config();
    config.setJdbcConfig(new JdbcConfig());
    config.getJdbcConfig().setUsername("JUNIT");
    configFileStore.save(config);

    Config result = JaxbUtil.unmarshal(Files.newInputStream(path), Config.class);

    assertThat(result.getJdbcConfig().getUsername()).isEqualTo("JUNIT");
  }
}
