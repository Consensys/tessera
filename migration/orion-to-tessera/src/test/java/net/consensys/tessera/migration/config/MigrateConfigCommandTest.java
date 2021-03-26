package net.consensys.tessera.migration.config;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.JaxbUtil;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MigrateConfigCommandTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    private TesseraJdbcOptions tesseraJdbcOptions;

    @Before
    public void beforeTest() {
        tesseraJdbcOptions = mock(TesseraJdbcOptions.class);
        when(tesseraJdbcOptions.getUrl()).thenReturn("jdbc:bogus:url");
    }

    @Test
    public void loadFullConfigSample() throws Exception {

        Path orionConfigFile = loadFromClassloader("/fullConfigTest.toml");

        MigrateConfigCommand migrateConfigCommand = new MigrateConfigCommand(
            orionConfigFile,
            outputDir.getRoot().toPath().resolve("tessera-config.json"),
            false,tesseraJdbcOptions
        );

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

        assertThat(keyData.getPrivateKeyPath().toAbsolutePath()).isEqualTo(Paths.get("").toAbsolutePath()
            .resolve("data").resolve("keys").resolve("tm1.key"));

        assertThat(keyData.getPublicKeyPath().toAbsolutePath()).isEqualTo(Paths.get("").toAbsolutePath()
            .resolve("data").resolve("keys").resolve("tm1.pub"));


        assertThat(keyConfiguration.getPasswordFile()).isEqualTo(Paths.get("").toAbsolutePath()
            .resolve("data").resolve("password.txt"));

        ServerConfig q2tServerConfig = config.getServerConfigs().stream()
            .filter(sc -> sc.getApp() == AppType.Q2T).findFirst().get();


        assertThat(q2tServerConfig.getServerUri()).isEqualTo(URI.create("http://127.0.0.1:9002"));
        assertThat(q2tServerConfig.getBindingUri()).isEqualTo(URI.create("http://0.0.0.0:9002"));

        ServerConfig p2pServerConfig = config.getServerConfigs().stream()
            .filter(sc -> sc.getApp() == AppType.P2P).findFirst().get();

        assertThat(p2pServerConfig.getServerUri()).isEqualTo(URI.create("http://127.0.0.1:9001"));
        assertThat(p2pServerConfig.getBindingUri()).isEqualTo(URI.create("http://0.0.0.0:9001"));

        JaxbUtil.marshalWithNoValidation(config,System.out);

    }

    @Test
    public void loadMinimalConfigSample() throws Exception {

        Path orionConfigFile = loadFromClassloader("/minimal-sample.toml");

        MigrateConfigCommand migrateConfigCommand = new MigrateConfigCommand(
            orionConfigFile,
            outputDir.getRoot().toPath().resolve("tessera-config.json"),
            false,
            tesseraJdbcOptions
        );

        Config config = migrateConfigCommand.call();
        assertThat(config).isNotNull();
        assertThat(config.getEncryptor().getType()).isEqualTo(EncryptorType.NACL);
        assertThat(config.isBootstrapNode()).isFalse();
        assertThat(config.isDisablePeerDiscovery()).isFalse();
        assertThat(config.isUseWhiteList()).isFalse();

        JaxbUtil.marshalWithNoValidation(config,System.out);
    }

    @Test
    public void pathResolutionSampleFromIssueRaised() throws Exception {

        Path orionConfigFile = loadFromClassloader("/path-resolution-sample.conf");
        MigrateConfigCommand migrateConfigCommand = new MigrateConfigCommand(
            orionConfigFile,
            outputDir.getRoot().toPath().resolve("tessera-config.json"),
            false,
            tesseraJdbcOptions
        );

        Config config = migrateConfigCommand.call();
        JaxbUtil.marshalWithNoValidation(config,System.out);

        List<KeyData> keys = config.getKeys().getKeyData();
        assertThat(keys).hasSize(1);

        KeyData keyData = keys.iterator().next();

        assertThat(keyData.getPrivateKeyPath().toAbsolutePath()).isEqualTo(Paths.get("").toAbsolutePath()
            .resolve("workdir/orion1").resolve("nodeKey.key"));

        assertThat(keyData.getPublicKeyPath().toAbsolutePath()).isEqualTo(Paths.get("").toAbsolutePath()
            .resolve("workdir/orion1").resolve("nodeKey.pub"));


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
