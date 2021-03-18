package net.consensys.tessera.migration.config;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.util.JaxbUtil;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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

        List<KeyData> keys = config.getKeys().getKeyData();
        assertThat(keys).hasSize(1);
        KeyData keyData = keys.iterator().next();

        assertThat(keyData.getPrivateKeyPath().toAbsolutePath()).isEqualTo(Paths.get("").toAbsolutePath()
            .resolve("data").resolve("keys").resolve("tm1.key"));

        assertThat(keyData.getPublicKeyPath().toAbsolutePath()).isEqualTo(Paths.get("").toAbsolutePath()
            .resolve("data").resolve("keys").resolve("tm1.pub"));

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
