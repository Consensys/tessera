package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.migration.test.FixtureUtil;
import com.quorum.tessera.config.util.KeyDataUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigBuilderTest {

    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    private final ConfigBuilder builderWithValidValues = FixtureUtil.builderWithValidValues();

    @Test
    public void nullIsNullAndNotAStringWithTheValueOfNull() {
        assertThat(ConfigBuilder.toPath(null, null)).isNull();
        assertThat(ConfigBuilder.toPath(null, "test")).isNotNull();
    }

    @Test
    public void buildValid() {
        final Config result = this.builderWithValidValues.build();

        assertThat(result).isNotNull();

        final ServerConfig unixServer =
            result.getServerConfigs().stream().filter(s -> s.getApp() == AppType.Q2T).findAny().get();
        assertThat(unixServer.getServerAddress()).isEqualTo("unix:" + Paths.get("somepath.ipc").toAbsolutePath());

        assertThat(result.getKeys().getKeyData()).hasSize(1);

        KeyEncryptorFactory keyEncryptorFactory = KeyEncryptorFactory.newFactory();
        KeyEncryptor keyEncryptor = keyEncryptorFactory.create(new EncryptorConfig() {{
            setType(EncryptorType.NACL);
        }});

        final ConfigKeyPair keyData = result.getKeys().getKeyData()
            .stream()
            .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
            .findFirst()
            .get();

        assertThat(keyData).isNotNull().extracting("privateKeyPath").containsExactly(Paths.get("private"));
        assertThat(keyData).isNotNull().extracting("publicKeyPath").containsExactly(Paths.get("public"));

        final ServerConfig serverConfig = result.getP2PServerConfig();
        assertThat(serverConfig).isNotNull();
        assertThat(serverConfig.getServerAddress()).isEqualTo("http://bogus.com:892");
        assertThat(serverConfig.getBindingAddress()).isEqualTo("http://bogus.com:892");

        final SslConfig sslConfig = serverConfig.getSslConfig();
        assertThat(sslConfig).isNotNull();
        assertThat(sslConfig.getClientKeyStorePassword()).isEqualTo("sslClientKeyStorePassword");
        assertThat(sslConfig.getClientKeyStore()).isEqualTo(Paths.get("sslClientKeyStorePath"));
        assertThat(sslConfig.getClientTlsKeyPath()).isEqualTo(Paths.get("sslClientTlsKeyPath"));
        assertThat(sslConfig.getServerTrustCertificates()).containsExactly(Paths.get("sslServerTrustCertificates"));

        assertThat(result.getJdbcConfig().getUsername()).isEqualTo("jdbcUsername");
        assertThat(result.getJdbcConfig().getPassword()).isEqualTo("jdbcPassword");
        assertThat(result.getJdbcConfig().getUrl()).isEqualTo("jdbc:bogus");
    }

    @Test
    public void influxHostNameEmptyThenInfluxConfigIsNull() {
        final Config result = builderWithValidValues.build();

        assertThat(result.getP2PServerConfig().getInfluxConfig()).isNull();
    }

    @Test
    public void alwaysSendToFileNotFoundPrintsErrorMessageToTerminal() {
        List<String> alwaysSendTo = new ArrayList<>();
        alwaysSendTo.add("doesntexist.txt");
        alwaysSendTo.add("alsodoesntexist.txt");

        final ConfigBuilder builder = builderWithValidValues.alwaysSendTo(alwaysSendTo);
        builder.build();

        assertThat(systemErrRule.getLog())
            .isEqualTo("Error reading alwayssendto file: doesntexist.txt\nError reading alwayssendto file: alsodoesntexist.txt\n");
    }

    @Test
    public void buildWithNoValuesSetDoesNotThrowException() {
        final ConfigBuilder builder = ConfigBuilder.create();
        final Config config = builder.build();

        assertThat(config).isNotNull();
    }
}
