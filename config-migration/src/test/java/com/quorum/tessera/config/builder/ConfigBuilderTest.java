package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.migration.test.FixtureUtil;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigBuilderTest {

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

        assertThat(result.getUnixSocketFile()).isEqualTo(Paths.get("somepath.ipc"));

        assertThat(result.getKeys().getKeyData()).hasSize(1);
        final ConfigKeyPair keyData = result.getKeys().getKeyData().get(0);
        assertThat(keyData).isNotNull().extracting("privateKeyPath").containsExactly(Paths.get("private"));
        assertThat(keyData).isNotNull().extracting("publicKeyPath").containsExactly(Paths.get("public"));

        final ServerConfig serverConfig = result.getServerConfig();
        assertThat(serverConfig).isNotNull();
        assertThat(serverConfig.getPort()).isEqualTo(892);
        assertThat(serverConfig.getHostName()).isEqualTo("http://bogus.com");
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

        assertThat(result.getServerConfig().getInfluxConfig()).isNull();
    }


    @Test
    public void alwaysSendToFileNotFoundPrintsErrorMessageToTerminal() {
        List<String> alwaysSendTo = new ArrayList<>();
        alwaysSendTo.add("doesntexist.txt");
        alwaysSendTo.add("alsodoesntexist.txt");

        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream originalErr = System.err;

        System.setErr(new PrintStream(errContent));

        final ConfigBuilder builder = builderWithValidValues.alwaysSendTo(alwaysSendTo);
        builder.build();

        assertThat(errContent.toString()).isEqualTo(
            "Error reading alwayssendto file: doesntexist.txt\nError reading alwayssendto file: alsodoesntexist.txt\n"
        );

        System.setErr(originalErr);

    }

    @Test
    public void buildWithNoValuesSetDoesNotThrowException() {
        final ConfigBuilder builder = ConfigBuilder.create();
        final Config config = builder.build();

        assertThat(config).isNotNull();
    }

}
