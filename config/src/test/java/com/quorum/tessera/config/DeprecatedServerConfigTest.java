package com.quorum.tessera.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DeprecatedServerConfigTest {

    @Test
    public void createConfigsFromDeprecatedServerConfig() {

        DeprecatedServerConfig deprecatedServerConfig = new DeprecatedServerConfig();
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
    
    
    @Test
    public void createConfigsFromDeprecatedServerConfigWithGprcPort() {

        DeprecatedServerConfig deprecatedServerConfig = new DeprecatedServerConfig();
        deprecatedServerConfig.setHostName("somehost");
        deprecatedServerConfig.setGrpcPort(99);

        List<ServerConfig> results = DeprecatedServerConfig.from(deprecatedServerConfig, Optional.empty());


        assertThat(results)
            .hasSize(3)
            .allMatch(s -> s.getCommunicationType() == CommunicationType.GRPC)
            .allMatch(ServerConfig::isEnabled)
            .extracting(ServerConfig::getServerSocket)
            .allMatch(InetServerSocket.class::isInstance)
            .extracting(InetServerSocket.class::cast)
            .allMatch(s -> s.getHostName().equals("somehost"))
            .allMatch(s -> s.getPort() == 99);

    }
    
    
        @Test
    public void createConfigsFromDeprecatedServerConfigWithUnixSocketFile() {

        DeprecatedServerConfig deprecatedServerConfig = new DeprecatedServerConfig();
        
        Path unixSocketFile = Paths.get("unixSocketFile");
        
        List<ServerConfig> results = DeprecatedServerConfig.from(deprecatedServerConfig, 
            Optional.of(unixSocketFile));


        assertThat(results)
            .hasSize(3)
            .allMatch(s -> s.getCommunicationType() == CommunicationType.UNIX_SOCKET)
            .allMatch(ServerConfig::isEnabled)
            .extracting(ServerConfig::getServerSocket)
            .allMatch(UnixServerSocket.class::isInstance)
            .extracting(UnixServerSocket.class::cast)
            .allMatch(s -> s.getPath().equals("unixSocketFile"));

    }
}
