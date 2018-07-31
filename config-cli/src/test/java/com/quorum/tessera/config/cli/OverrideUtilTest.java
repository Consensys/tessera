package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class OverrideUtilTest {

    @Test
    public void buildOptions() throws Exception {

        List<String> expected = Arrays.asList(
                "jdbc.username",
                "jdbc.password",
                "jdbc.url",
                "server.hostName",
                "server.port",
                "server.sslConfig.tls",
                "server.sslConfig.generateKeyStoreIfNotExisted",
                "server.sslConfig.serverKeyStore",
                "server.sslConfig.serverTlsKeyPath",
                "server.sslConfig.serverTlsCertificatePath",
                "server.sslConfig.serverKeyStorePassword",
                "server.sslConfig.serverTrustStore",
                "server.sslConfig.serverTrustStorePassword",
                "server.sslConfig.serverTrustMode",
                "server.sslConfig.clientKeyStore",
                "server.sslConfig.clientTlsKeyPath",
                "server.sslConfig.clientTlsCertificatePath",
                "server.sslConfig.clientKeyStorePassword",
                "server.sslConfig.clientTrustStore",
                "server.sslConfig.clientTrustStorePassword",
                "server.sslConfig.clientTrustMode",
                "server.sslConfig.knownClientsFile",
                "server.sslConfig.knownServersFile",
                "server.influxConfig.hostName",
                "server.influxConfig.port",
                "server.influxConfig.dbName",
                "peer.url",
                "keys.passwordFile",
                "keys.passwords",
                "keys.keyData.config.data.bytes",
                "keys.keyData.config.data.snonce",
                "keys.keyData.config.data.asalt",
                "keys.keyData.config.data.sbox",
                "keys.keyData.config.data.aopts.algorithm",
                "keys.keyData.config.data.aopts.iterations",
                "keys.keyData.config.data.aopts.memory",
                "keys.keyData.config.data.aopts.parallelism",
                "keys.keyData.config.data.password",
                "keys.keyData.config.type",
                "keys.keyData.privateKey",
                "keys.keyData.publicKey",
                "keys.keyData.privateKeyPath",
                "keys.keyData.publicKeyPath",
                "alwaysSendTo.key",
                "unixSocketFile",
                "useWhiteList",
                "server.sslConfig.clientTrustCertificates",
                "server.sslConfig.serverTrustCertificates"
        );

        Map<String, Class> results = OverrideUtil.buildConfigOptions();

        assertThat(results.keySet())
                .filteredOn(s -> !s.contains("$jacocoData"))
                .containsExactlyInAnyOrderElementsOf(expected);

        assertThat(results.get("server.sslConfig.knownClientsFile")).isEqualTo(Path.class);
        assertThat(results.get("keys.passwords")).isEqualTo(String[].class);

    }

    @Test
    public void initialiseConfigFromNoValues() throws Exception {

        final Method factoryMethod = Config.class.getDeclaredMethod("create");
        factoryMethod.setAccessible(true);
        final Config config = (Config) factoryMethod.invoke(null);
        assertThat(config).isNotNull();

        OverrideUtil.overrideExistingValue(config, "jdbc.username", "someuser");
        OverrideUtil.overrideExistingValue(config, "server.hostName", "somehost");
        OverrideUtil.overrideExistingValue(config, "keys.passwords", "pw_one", "pw_two");
        
        
        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");
        assertThat(config.getServerConfig().getHostName()).isEqualTo("somehost");
        assertThat(config.getKeys().getPasswords()).contains("pw_one", "pw_two");
    }

}
