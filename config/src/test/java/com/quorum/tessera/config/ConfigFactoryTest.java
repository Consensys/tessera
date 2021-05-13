package com.quorum.tessera.config;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.quorum.tessera.test.util.ElUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class ConfigFactoryTest {

  @Test
  public void createFromSample() throws Exception {

    ConfigFactory configFactory = ConfigFactory.create();

    assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    InputStream configInputStream =
        ElUtil.process(getClass().getResourceAsStream("/sample.json"), params);

    Config config = configFactory.create(configInputStream);

    assertThat(config).isNotNull();
    assertThat(config.isUseWhiteList()).isFalse();
    assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");
    assertThat(config.getPeers()).hasSize(2);
    assertThat(config.getKeys().getKeyData()).hasSize(1);
    assertThat(config.getKeys().getKeyData().get(0)).isInstanceOf(KeyData.class);

    assertThat(config.getFeatures().isEnablePrivacyEnhancements()).isFalse();
    assertThat(config.getFeatures().isEnableRemoteKeyValidation()).isFalse();
    assertThat(config.getClientMode()).isEqualTo(ClientMode.TESSERA);
  }

  @Test
  public void createFromSampleJaxbException() {

    final ConfigFactory configFactory = ConfigFactory.create();
    assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

    final InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

    final Throwable throwable = catchThrowable(() -> configFactory.create(inputStream));
    assertThat(throwable).isInstanceOf(ConfigException.class);
  }

  @Test
  public void createFromKeyGenSample() throws Exception {

    final Path tempFolder =
        Files.createTempDirectory(UUID.randomUUID().toString()).toAbsolutePath();

    final ConfigFactory configFactory = ConfigFactory.create();
    assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

    Path unixSocketPath = Files.createTempFile(tempFolder, UUID.randomUUID().toString(), ".ipc");

    Map<String, Object> params = singletonMap("unixSocketPath", unixSocketPath.toString());

    InputStream configInputStream =
        ElUtil.process(getClass().getResourceAsStream("/sample-private-keygen.json"), params);

    Config config = configFactory.create(configInputStream);

    assertThat(config).isNotNull();
  }

  @Test
  public void createFromSampleV3() throws Exception {

    ConfigFactory configFactory = ConfigFactory.create();

    assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    InputStream configInputStream =
        ElUtil.process(getClass().getResourceAsStream("/sample_v3.json"), params);

    Config config = configFactory.create(configInputStream);

    assertThat(config).isNotNull();
    assertThat(config.isUseWhiteList()).isFalse();
    assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");
    assertThat(config.getPeers()).hasSize(2);
    assertThat(config.getKeys().getKeyData()).hasSize(1);
    assertThat(config.getKeys().getKeyData().get(0)).isInstanceOf(KeyData.class);

    assertThat(config.getFeatures().isEnablePrivacyEnhancements()).isTrue();
    assertThat(config.getFeatures().isEnableRemoteKeyValidation()).isTrue();
    assertThat(config.getClientMode()).isEqualTo(ClientMode.ORION);
  }

  @Test
  public void createFromSampleResidentGroup() throws IOException {

    ConfigFactory configFactory = ConfigFactory.create();

    assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    final InputStream configInputStream =
        ElUtil.process(getClass().getResourceAsStream("/sample_rg.json"), params);

    ResidentGroup expected1 = new ResidentGroup();
    expected1.setName("legacy");
    expected1.setDescription(
        "Privacy groups to support the creation of groups by privateFor and privateFrom");
    expected1.setMembers(
        List.of(
            "B687sgdtqsem2qEXO8h8UqvW1Mb3yKo7id5hPFLwCmY=",
            "arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg="));

    ResidentGroup expected2 = new ResidentGroup();
    expected2.setName("web3js-eea");
    expected2.setDescription("test");
    expected2.setMembers(
        List.of(
            "arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg=",
            "B687sgdtqsem2qEXO8h8UqvW1Mb3yKo7id5hPFLwCmY="));

    Config config = configFactory.create(configInputStream);

    assertThat(config).isNotNull();

    assertThat(config.getResidentGroups()).isNotEmpty();
    assertThat(config.getResidentGroups()).hasSize(2);
    assertThat(config.getResidentGroups()).containsExactly(expected1, expected2);
  }
}
