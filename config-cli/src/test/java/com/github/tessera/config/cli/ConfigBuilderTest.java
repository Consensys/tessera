package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.SslAuthenticationMode;
import com.github.tessera.config.SslTrustMode;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class ConfigBuilderTest {

    private final ConfigBuilder builderWithValidValues = ConfigBuilder.create()
            .jdbcUrl("jdbc:bogus")
            .jdbcUsername("jdbcUsername")
            .jdbcPassword("jdbcPassword")
            .peers(Collections.EMPTY_LIST)
            .serverPort(892)
            .sslAuthenticationMode(SslAuthenticationMode.STRICT)
            .unixSocketFile("somepath.ipc")
            .serverUri("http://bogus.com:928")
            
            .sslServerKeyStorePath("sslServerKeyStorePath")
            .sslServerTrustMode(SslTrustMode.TOFU)
            .sslServerTrustStorePath("sslServerTrustStorePath")
            
            .sslServerCertificate("sslServerCertificate")
            .sslServerTrustStorePath("sslServerKeyStorePath")
            .sslClientKeyStorePath("sslClientKeyStorePath")
            .sslClientTrustStorePath("sslClientTrustStorePath")
            .sslClientKeyStorePassword("sslClientKeyStorePassword")
            .sslClientTrustStorePassword("sslClientTrustStorePassword")
            
            .knownClientsFile("knownClientsFile")
            .knownServersFile("knownServersFile");

    @Test
    public void buildValid() {
        Config result = builderWithValidValues.build();

        assertThat(result).isNotNull();
    }


    @Test
    public void buildFromExisting() {
        Config existing = builderWithValidValues.build();
        
        ConfigBuilder configBuilder = ConfigBuilder.from(existing);
        
        Config result = configBuilder.build();
        
        assertThat(result).isNotNull();
        
        
    }
    
}
