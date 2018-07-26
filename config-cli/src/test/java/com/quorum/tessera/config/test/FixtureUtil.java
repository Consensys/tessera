package com.quorum.tessera.config.test;

import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.builder.ConfigBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Arrays;

public class FixtureUtil {

    private static final JsonObject LOCKED_PRIVATE_KEY_DATA = Json.createObjectBuilder()
            .add("data", Json.createObjectBuilder()
                    .add("aopts",
                            Json.createObjectBuilder()
                                    .add("variant", "id")
                                    .add("memory", 1048576)
                                    .add("iterations", 10)
                                    .add("parallelism", 4)
                                    .add("version", 1.3)
                    )
                    .add("snonce", "xx3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC")
                    .add("asalt", "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=")
                    .add("sbox", "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc").build())
            .add("type", "argon2sbox")
            .build();

    public static JsonObject createLockedPrivateKey() {
        return LOCKED_PRIVATE_KEY_DATA;
    }

    public static ConfigBuilder builderWithValidValues() {

        return ConfigBuilder.create()
                .jdbcConfig(new JdbcConfig("jdbcUsername", "jdbcPassword", "jdbc:bogus"))
                .peers(Arrays.asList("http://one.com:8989/one", "http://two.com:9929/two"))
                .serverPort(892)
                .sslAuthenticationMode(SslAuthenticationMode.STRICT)
                .unixSocketFile("somepath.ipc")
                .serverHostname("http://bogus.com:928")
                .sslServerKeyStorePath("sslServerKeyStorePath")
                .sslServerTrustMode(SslTrustMode.TOFU)
                .sslServerTrustStorePath("sslServerTrustStorePath")
                .sslServerTrustStorePath("sslServerKeyStorePath")
                .sslClientKeyStorePath("sslClientKeyStorePath")
                .sslClientTrustStorePath("sslClientTrustStorePath")
                .sslClientKeyStorePassword("sslClientKeyStorePassword")
                .sslClientTrustStorePassword("sslClientTrustStorePassword")
                .sslClientTrustMode(SslTrustMode.CA_OR_TOFU)
                .knownClientsFile("knownClientsFile")
                .knownServersFile("knownServersFile");
    }

}
