package com.quorum.tessera.config.migration.test;

import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;

import javax.json.Json;
import javax.json.JsonObject;
import java.nio.file.Paths;
import java.util.Collections;

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

        return ConfigBuilder.create().jdbcConfig(new JdbcConfig("jdbcUsername", "jdbcPassword", "jdbc:bogus"))
            .peers(Collections.emptyList())
            .alwaysSendTo(Collections.emptyList())
            .serverPort(892)
            .sslAuthenticationMode(SslAuthenticationMode.STRICT)
            .unixSocketFile("somepath.ipc")
            .serverHostname("http://bogus.com")
            .sslServerKeyStorePath("sslServerKeyStorePath")
            .sslServerTrustMode(SslTrustMode.TOFU)
            .sslServerTrustStorePath("sslServerTrustStorePath")
            .sslServerTrustStorePath("sslServerKeyStorePath")
            .sslClientKeyStorePath("sslClientKeyStorePath")
            .sslClientTrustStorePath("sslClientTrustStorePath")
            .sslClientKeyStorePassword("sslClientKeyStorePassword")
            .sslClientTrustStorePassword("sslClientTrustStorePassword")
            .sslServerTlsKeyPath("sslServerTlsKeyPath")
            .sslClientTlsKeyPath("sslClientTlsKeyPath")
            .sslKnownClientsFile("knownClientsFile")
            .sslKnownServersFile("knownServersFile")
            .sslClientTrustMode(SslTrustMode.CA_OR_TOFU)
            .sslServerTrustCertificates(Collections.singletonList("sslServerTrustCertificates"))
            .sslClientTrustCertificates(Collections.singletonList("sslClientTrustCertificates"))
            .sslClientTlsCertificatePath("sslClientTlsCertificatePath")
            .sslServerTlsCertificatePath("sslServerTlsCertificatePath")
            .keyData(new KeyConfiguration(null, Collections.emptyList(),
                Collections.singletonList(new FilesystemKeyPair(Paths.get("public"), Paths.get("private")))));
    }

    public static ConfigBuilder builderWithNullValues() {

        return ConfigBuilder.create().jdbcConfig(new JdbcConfig("jdbcUsername", "jdbcPassword", "jdbc:bogus"))
            .peers(Collections.emptyList())
            .alwaysSendTo(Collections.emptyList())
            .serverPort(892)
            .sslAuthenticationMode(SslAuthenticationMode.STRICT)
            .unixSocketFile("somepath.ipc")
            .serverHostname("http://bogus.com")
            .sslServerKeyStorePath(null)
            .sslServerTrustMode(SslTrustMode.TOFU)
            .sslServerTrustStorePath("sslServerTrustStorePath")
            .sslServerTrustStorePath("sslServerKeyStorePath")
            .sslClientKeyStorePath("sslClientKeyStorePath")
            .sslClientTrustStorePath("sslClientTrustStorePath")
            .sslClientKeyStorePassword("sslClientKeyStorePassword")
            .sslClientTrustStorePassword("sslClientTrustStorePassword")
            .sslServerTlsKeyPath("sslServerTlsKeyPath")
            .sslClientTlsKeyPath("sslClientTlsKeyPath")
            .sslKnownClientsFile("knownClientsFile")
            .sslKnownServersFile(null)
            .sslClientTrustMode(SslTrustMode.CA_OR_TOFU)
            .sslServerTrustCertificates(Collections.singletonList("sslServerTrustCertificates"))
            .sslClientTrustCertificates(Collections.singletonList("sslClientTrustCertificates"))
            .sslClientTlsCertificatePath("sslClientTlsCertificatePath")
            .sslServerTlsCertificatePath("sslServerTlsCertificatePath")
            .keyData(new KeyConfiguration(null, Collections.emptyList(),
                Collections.singletonList(new FilesystemKeyPair(Paths.get("public"), Paths.get("private")))));
    }
    
    public static JsonObject createUnlockedPrivateKey() {
        return Json.createObjectBuilder()
            .add("data", Json.createObjectBuilder()
                    .add("snonce", "xx3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC")
                    .add("asalt", "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=")
                    .add("sbox", "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc").build())
            .add("type", "unlocked")
            .build();
    }
    
}
