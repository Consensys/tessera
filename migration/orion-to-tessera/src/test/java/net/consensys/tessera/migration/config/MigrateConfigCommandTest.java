package net.consensys.tessera.migration.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MigrateConfigCommandTest {

  @Rule public TemporaryFolder outputDir = new TemporaryFolder();

  private TesseraJdbcOptions tesseraJdbcOptions;

  @Before
  public void beforeTest() {
    tesseraJdbcOptions = mock(TesseraJdbcOptions.class);
    when(tesseraJdbcOptions.getUrl()).thenReturn("jdbc:bogus:url");
  }

  @Test
  public void loadFullConfigSample() throws Exception {

    Path orionConfigFile = loadFromClassloader("/fullConfigTest.toml");

    MigrateConfigCommand migrateConfigCommand =
        new MigrateConfigCommand(
            orionConfigFile,
            outputDir.getRoot().toPath().resolve("tessera-config.json"),
            tesseraJdbcOptions);

    Config config = migrateConfigCommand.call();

    assertThat(config).isNotNull();
    assertThat(config.getEncryptor().getType()).isEqualTo(EncryptorType.NACL);
    assertThat(config.isBootstrapNode()).isFalse();
    assertThat(config.isDisablePeerDiscovery()).isFalse();
    assertThat(config.isUseWhiteList()).isFalse();

    assertThat(config.getServerConfigs()).hasSize(2);

    KeyConfiguration keyConfiguration = config.getKeys();

    List<KeyData> keys = keyConfiguration.getKeyData();
    assertThat(keys).hasSize(1);
    KeyData keyData = keys.iterator().next();

    assertThat(keyData.getPrivateKeyPath().toAbsolutePath())
        .isEqualTo(
            Paths.get("").toAbsolutePath().resolve("data").resolve("keys").resolve("tm1.key"));

    assertThat(keyData.getPublicKeyPath().toAbsolutePath())
        .isEqualTo(
            Paths.get("").toAbsolutePath().resolve("data").resolve("keys").resolve("tm1.pub"));

    assertThat(keyConfiguration.getPasswordFile())
        .isEqualTo(
            Paths.get("").toAbsolutePath().resolve("data").resolve("keys").resolve("password.txt"));

    ServerConfig q2tServerConfig =
        config.getServerConfigs().stream()
            .filter(sc -> sc.getApp() == AppType.Q2T)
            .findFirst()
            .get();

    assertThat(q2tServerConfig.getServerUri()).isEqualTo(URI.create("http://127.0.0.1:9002"));
    assertThat(q2tServerConfig.getBindingUri()).isEqualTo(URI.create("http://0.0.0.0:9002"));

    ServerConfig p2pServerConfig =
        config.getServerConfigs().stream()
            .filter(sc -> sc.getApp() == AppType.P2P)
            .findFirst()
            .get();

    assertThat(p2pServerConfig.getServerUri()).isEqualTo(URI.create("http://127.0.0.1:9001"));
    assertThat(p2pServerConfig.getBindingUri()).isEqualTo(URI.create("http://0.0.0.0:9001"));

    // Assert SSL related values
    final SslConfig p2pSslConfig = p2pServerConfig.getSslConfig();

    assertThat(p2pSslConfig.getTls()).isEqualTo(SslAuthenticationMode.STRICT);
    assertThat(p2pSslConfig.getServerTlsKeyPath().getFileName().toString())
        .isEqualTo("server-key.pem");
    assertThat(p2pSslConfig.getServerTlsCertificatePath().getFileName().toString())
        .isEqualTo("server-cert.pem");
    assertThat(p2pSslConfig.getServerTrustCertificates()).hasSize(2);
    assertThat(p2pSslConfig.getServerTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);
    assertThat(p2pSslConfig.getKnownClientsFile().getFileName().toString())
        .isEqualTo("known-clients");

    assertThat(p2pSslConfig.getClientTlsKeyPath().getFileName().toString())
        .isEqualTo("client-key.pem");
    assertThat(p2pSslConfig.getClientTlsCertificatePath().getFileName().toString())
        .isEqualTo("client-cert.pem");
    assertThat(p2pSslConfig.getClientTrustCertificates()).hasSize(1);
    assertThat(p2pSslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA);
    assertThat(p2pSslConfig.getKnownServersFile().getFileName().toString())
        .isEqualTo("known-servers");

    final SslConfig q2tSslConfig = q2tServerConfig.getSslConfig();
    assertThat(q2tSslConfig.getTls()).isEqualTo(SslAuthenticationMode.OFF);

    assertThat(q2tSslConfig.getServerTlsKeyPath().getFileName().toString()).isEqualTo("key.pem");
    assertThat(q2tSslConfig.getServerTlsCertificatePath().getFileName().toString())
        .isEqualTo("client-presented-cert.pem");
    assertThat(q2tSslConfig.getServerTrustCertificates()).hasSize(0);
    assertThat(q2tSslConfig.getServerTrustMode()).isEqualTo(SslTrustMode.WHITELIST);
    assertThat(q2tSslConfig.getKnownClientsFile().getFileName().toString())
        .isEqualTo("client-connection-known-clients");

    JaxbUtil.marshalWithNoValidation(config, System.out);
  }

  @Test
  public void loadMinimalConfigSample() throws Exception {

    Path orionConfigFile = loadFromClassloader("/minimal-sample.toml");

    MigrateConfigCommand migrateConfigCommand =
        new MigrateConfigCommand(
            orionConfigFile,
            outputDir.getRoot().toPath().resolve("tessera-config.json"),
            tesseraJdbcOptions);

    Config config = migrateConfigCommand.call();
    assertThat(config).isNotNull();
    assertThat(config.getEncryptor().getType()).isEqualTo(EncryptorType.NACL);
    assertThat(config.isBootstrapNode()).isFalse();
    assertThat(config.isDisablePeerDiscovery()).isFalse();
    assertThat(config.isUseWhiteList()).isFalse();

    JaxbUtil.marshalWithNoValidation(config, System.out);
  }

  @Test
  public void pathResolutionSampleFromIssueRaised() throws Exception {

    Path orionConfigFile = loadFromClassloader("/path-resolution-sample.conf");
    MigrateConfigCommand migrateConfigCommand =
        new MigrateConfigCommand(
            orionConfigFile,
            outputDir.getRoot().toPath().resolve("tessera-config.json"),
            tesseraJdbcOptions);

    Config config = migrateConfigCommand.call();
    JaxbUtil.marshalWithNoValidation(config, System.out);

    List<KeyData> keys = config.getKeys().getKeyData();
    assertThat(keys).hasSize(1);

    KeyData keyData = keys.iterator().next();

    assertThat(keyData.getPrivateKeyPath().toAbsolutePath())
        .isEqualTo(Paths.get("").toAbsolutePath().resolve("workdir/orion1").resolve("nodeKey.key"));

    assertThat(keyData.getPublicKeyPath().toAbsolutePath())
        .isEqualTo(Paths.get("").toAbsolutePath().resolve("workdir/orion1").resolve("nodeKey.pub"));
  }

  @Test
  public void minimalSslConfigAssertDefaultValues() throws IOException {

    Path orionConfigFile = loadFromClassloader("/minimal-ssl.toml");

    MigrateConfigCommand migrateConfigCommand =
        new MigrateConfigCommand(
            orionConfigFile,
            outputDir.getRoot().toPath().resolve("tessera-config.json"),
            tesseraJdbcOptions);

    Config config = migrateConfigCommand.call();
    JaxbUtil.marshalWithNoValidation(config, System.out);

    final SslConfig p2pSslConfig = config.getP2PServerConfig().getSslConfig();

    assertThat(p2pSslConfig.getTls()).isEqualTo(SslAuthenticationMode.STRICT);
    assertThat(p2pSslConfig.getServerTlsKeyPath().getFileName().toString())
        .isEqualTo("tls-server-key.pem");
    assertThat(p2pSslConfig.getServerTlsCertificatePath().getFileName().toString())
        .isEqualTo("tls-server-cert.pem");

    assertThat(p2pSslConfig.getServerTrustMode()).isEqualTo(SslTrustMode.TOFU);
    assertThat(p2pSslConfig.getKnownClientsFile().getFileName().toString())
        .isEqualTo("tls-known-clients");

    assertThat(p2pSslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);
    assertThat(p2pSslConfig.getKnownServersFile().getFileName().toString())
        .isEqualTo("tls-known-servers");
  }

  static Path loadFromClassloader(String path) {
    URL url = MigrateConfigCommandTest.class.getResource(path);
    try {
      return Paths.get(url.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
