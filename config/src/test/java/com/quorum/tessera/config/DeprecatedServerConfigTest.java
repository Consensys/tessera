package com.quorum.tessera.config;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DeprecatedServerConfigTest {

    @Test
    public void createConfigsFromDeprecatedServerConfig() {

        DeprecatedServerConfig deprecatedServerConfig = new DeprecatedServerConfig();
        deprecatedServerConfig.setCommunicationType(CommunicationType.REST);
        deprecatedServerConfig.setHostName("somehost");
        deprecatedServerConfig.setPort(99);

        List<ServerConfig> results = DeprecatedServerConfig.from(deprecatedServerConfig, Optional.empty());


        assertThat(results)
            .hasSize(3)
            .allMatch(s -> s.getCommunicationType() == CommunicationType.REST)
            .allMatch(ServerConfig::isEnabled)
            .extracting(ServerConfig::getServerSocket)
            .allMatch(InetServerSocket.class::isInstance)
            .extracting(InetServerSocket.class::cast)
            .allMatch(s -> s.getHostName().equals("somehost"))
            .allMatch(s -> s.getPort() == 99);
        
        
        
        
    }

}
