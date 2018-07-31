package com.quorum.tessera.config.cli;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class OverrideUtilTest {

    @Test
    public void buildOptions() throws Exception {

        List<String> expected = Arrays.asList(
                "config.jdbcConfig.username",
                "config.jdbcConfig.password",
                "config.jdbcConfig.url",
                "config.serverConfig.hostName",
                "config.serverConfig.port",
                "config.serverConfig.sslConfig.tls",
                "config.serverConfig.sslConfig.generateKeyStoreIfNotExisted",
                "config.serverConfig.sslConfig.serverKeyStore",
                "config.serverConfig.sslConfig.serverTlsKeyPath",
                "config.serverConfig.sslConfig.serverTlsCertificatePath",
                "config.serverConfig.sslConfig.serverKeyStorePassword",
                "config.serverConfig.sslConfig.serverTrustStore",
                "config.serverConfig.sslConfig.serverTrustStorePassword",
                "config.serverConfig.sslConfig.serverTrustMode",
                "config.serverConfig.sslConfig.clientKeyStore",
                "config.serverConfig.sslConfig.clientTlsKeyPath",
                "config.serverConfig.sslConfig.clientTlsCertificatePath",
                "config.serverConfig.sslConfig.clientKeyStorePassword",
                "config.serverConfig.sslConfig.clientTrustStore",
                "config.serverConfig.sslConfig.clientTrustStorePassword",
                "config.serverConfig.sslConfig.clientTrustMode",
                "config.serverConfig.sslConfig.knownClientsFile",
                "config.serverConfig.sslConfig.knownServersFile",
                "config.serverConfig.influxConfig.hostName",
                "config.serverConfig.influxConfig.port",
                "config.serverConfig.influxConfig.dbName",
                "config.peers[].url",
                "config.keys.passwordFile",
                "config.keys.passwords[].value",
                "config.keys.passwords[].hash",
                "config.keys.passwords[].serialVersionUID",
                "config.keys.passwords[].serialPersistentFields",
                "config.keys.keyData[].config.privateKeyData.value",
                "config.keys.keyData[].config.privateKeyData.snonce",
                "config.keys.keyData[].config.privateKeyData.asalt",
                "config.keys.keyData[].config.privateKeyData.sbox",
                "config.keys.keyData[].config.privateKeyData.argonOptions.algorithm",
                "config.keys.keyData[].config.privateKeyData.argonOptions.iterations",
                "config.keys.keyData[].config.privateKeyData.argonOptions.memory",
                "config.keys.keyData[].config.privateKeyData.argonOptions.parallelism",
                "config.keys.keyData[].config.privateKeyData.password",
                "config.keys.keyData[].config.type",
                "config.keys.keyData[].privateKey",
                "config.keys.keyData[].publicKey",
                "config.keys.keyData[].privateKeyPath",
                "config.keys.keyData[].publicKeyPath",
                "config.fowardingList[].key",
                "config.unixSocketFile",
                "config.useWhiteList"
        );

        List<String> results = OverrideUtil.buildConfigOptions();

        assertThat(results)
                .filteredOn(s -> !s.contains("$jacocoData"))
                .containsExactlyInAnyOrderElementsOf(expected);

    }

}
