package com.quorum.tessera.config;

import com.quorum.tessera.config.util.JaxbUtil;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ConfigMaskingTest {

    @Test
    public void marshal() {

        JdbcConfig jdbcConfig = new JdbcConfig("user", "password", "url");

        List<Peer> peers = Collections.EMPTY_LIST;
        KeyData keyData = new KeyData(null, "privateKey", "publicKey", null, null);

        KeyConfiguration keyConfiguration = new KeyConfiguration(null, Collections.EMPTY_LIST, Arrays.asList(keyData));

        SslConfig sslConfig = new SslConfig(SslAuthenticationMode.STRICT, false,
                Paths.get("serverKeyStore"),
                "serverKeyStorePassword",
                Paths.get("serverTrustStore"),
                "serverTrustStorePassword",
                SslTrustMode.WHITELIST,
                Paths.get("clientKeyStore"),
                "clientKeyStorePassword",
                Paths.get("clientTrustStore"),
                "clientTrustStorePassword",
                SslTrustMode.WHITELIST,
                Paths.get("knownClientsFile"),
                Paths.get("knownServersFile"),
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Paths.get("serverTlsKeyPath"),
                Paths.get("serverTlsCertificatePath"),
                Paths.get("clientTlsKeyPath"),
                Paths.get("clientTlsCertificatePath"));

        ServerConfig serverConfig = new ServerConfig("hostName", Integer.SIZE, sslConfig, null);
        Config config = new Config(jdbcConfig, serverConfig, peers, keyConfiguration,
                Collections.EMPTY_LIST, Paths.get("unixSocket"), true);

        JsonObject result = Stream.of(config)
                .map(JaxbUtil::marshalToStringNoValidation)
                .map(StringReader::new)
                .map(Json::createReader)
                .map(JsonReader::readObject)
                .findFirst().get();

        assertThat(result.getJsonObject("jdbc").getString("password")).isEqualTo("********");
        assertThat(result.getJsonObject("keys")
                .getJsonArray("keyData")
                .getJsonObject(0).getString("privateKey"))
                .isEqualTo("**********");

        assertThat(result.getJsonObject("server")
                .getJsonObject("sslConfig")
                .getString("clientTrustStorePassword"))
                .isEqualTo("************************");

        assertThat(result.getJsonObject("server")
                .getJsonObject("sslConfig")
                .getString("clientKeyStorePassword"))
                .isEqualTo("**********************");

        assertThat(result.getJsonObject("server")
                .getJsonObject("sslConfig")
                .getString("serverKeyStorePassword"))
                .isEqualTo("**********************");

        assertThat(result.getJsonObject("server")
                .getJsonObject("sslConfig")
                .getString("serverTrustStorePassword"))
                .isEqualTo("************************");

    }

}
