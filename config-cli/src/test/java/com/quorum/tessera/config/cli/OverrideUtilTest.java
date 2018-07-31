package com.quorum.tessera.config.cli;

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
                "server.ssl.tls",
                "server.ssl.generateKeyStoreIfNotExisted",
                "server.ssl.serverKeyStore",
                "server.ssl.serverTlsKeyPath",
                "server.ssl.serverTlsCertificatePath",
                "server.ssl.serverKeyStorePassword",
                "server.ssl.serverTrustStore",
                "server.ssl.serverTrustStorePassword",
                "server.ssl.serverTrustMode",
                "server.ssl.clientKeyStore",
                "server.ssl.clientTlsKeyPath",
                "server.ssl.clientTlsCertificatePath",
                "server.ssl.clientKeyStorePassword",
                "server.ssl.clientTrustStore",
                "server.ssl.clientTrustStorePassword",
                "server.ssl.clientTrustMode",
                "server.ssl.knownClientsFile",
                "server.ssl.knownServersFile",
                "server.influx.hostName",
                "server.influx.port",
                "server.influx.dbName",
                "peer[].url",
                "keys.passwordFile",
                "keys.passwords[]",
                "keys.keyData[].config.data.bytes",
                "keys.keyData[].config.data.snonce",
                "keys.keyData[].config.data.asalt",
                "keys.keyData[].config.data.sbox",
                "keys.keyData[].config.data.aopts.algorithm",
                "keys.keyData[].config.data.aopts.iterations",
                "keys.keyData[].config.data.aopts.memory",
                "keys.keyData[].config.data.aopts.parallelism",
                "keys.keyData[].config.data.password",
                "keys.keyData[].config.type",
                "keys.keyData[].privateKey",
                "keys.keyData[].publicKey",
                "keys.keyData[].privateKeyPath",
                "keys.keyData[].publicKeyPath",
                "alwaysSendTo[].key",
                "unixSocketFile",
                "useWhiteList",
                "server.ssl.clientTrustCertificates[]",
                "server.ssl.serverTrustCertificates[]"
        );

        Map<String,Class> results = OverrideUtil.buildConfigOptions();

        assertThat(results.keySet())
                .filteredOn(s -> !s.contains("$jacocoData"))
                .containsExactlyInAnyOrderElementsOf(expected);

                assertThat(results.get("server.ssl.knownClientsFile")).isEqualTo(Path.class);
                assertThat(results.get("keys.passwords[]")).isEqualTo(String.class);
        
    }

}
