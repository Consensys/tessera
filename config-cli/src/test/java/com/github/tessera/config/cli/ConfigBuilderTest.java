package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.SslAuthenticationMode;
import com.github.tessera.config.SslTrustMode;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ConfigBuilderTest {
    
    @Test
    public void buildValid() {
        Config result = ConfigBuilder.create()
                .jdbcUrl("jdbc:bogus")
                .peers(Collections.EMPTY_LIST)
                .serverPort(892)
                .sslAuthenticationMode(SslAuthenticationMode.STRICT)
                .unixSocketFile("somepath.ipc")
                .serverUri("http://bogus.com:928:/path")
                .sslServerKeyStorePath("sslServerKeyStorePath")
                .sslServerTrustMode(SslTrustMode.TOFU)
                .sslServerTrustStorePath("sslServerTrustStorePath")
                .sslServerCertificate("sslServerCertificate")
                .sslServerTrustStorePath("sslServerKeyStorePath")
                .sslClientKeyStorePath("sslClientKeyStorePath")
                .sslClientTrustStorePath("sslClientTrustStorePath")
                .sslClientKeyStorePassword("sslClientKeyStorePassword")
                .knownClientsFile("knownClientsFile")
                .knownServersFile("knownServersFile")
                .build();
        
        assertThat(result).isNotNull();
    }
    
}
