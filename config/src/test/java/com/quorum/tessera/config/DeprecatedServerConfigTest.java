package com.quorum.tessera.config;

import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeprecatedServerConfigTest {

    @Test
    public void createConfigsFromDeprecatedServerConfigWithCommTypeRest() {

        DeprecatedServerConfig deprecatedServerConfig = new DeprecatedServerConfig();
        deprecatedServerConfig.setHostName("somehost");
        deprecatedServerConfig.setPort(99);
        deprecatedServerConfig.setCommunicationType(CommunicationType.REST);

        Path unixSocketFile = Paths.get("unixSocketFile");

        List<ServerConfig> results = DeprecatedServerConfig.from(deprecatedServerConfig, unixSocketFile);

        assertThat(results).hasSize(2);
        ServerConfig q2t = results.get(0);
        assertThat(q2t.getCommunicationType()).isEqualTo(CommunicationType.REST);
        assertThat(q2t.getServerUri()).isEqualTo(URI.create("unix:unixSocketFile"));
        assertThat(q2t.isEnabled()).isTrue();
        assertThat(q2t.getApp()).isEqualTo(AppType.Q2T);

        ServerConfig p2p = results.get(1);
        assertThat(p2p.getCommunicationType()).isEqualTo(CommunicationType.REST);
        assertThat(p2p.getServerUri()).isEqualTo(URI.create("somehost:99"));
        assertThat(p2p.isEnabled()).isTrue();
        assertThat(p2p.getApp()).isEqualTo(AppType.P2P);
    }


    @Test
    public void createConfigsFromDeprecatedServerConfigWithCommTypeGRPC() {

        DeprecatedServerConfig deprecatedServerConfig = new DeprecatedServerConfig();
        deprecatedServerConfig.setHostName("somehost");
        deprecatedServerConfig.setGrpcPort(99);

        deprecatedServerConfig.setCommunicationType(CommunicationType.GRPC);

        Path unixSocketFile = Paths.get("unixSocketFile");

        List<ServerConfig> results = DeprecatedServerConfig.from(deprecatedServerConfig, unixSocketFile);

        assertThat(results).hasSize(2);
        ServerConfig q2t = results.get(0);
        assertThat(q2t.getCommunicationType()).isEqualTo(CommunicationType.REST);
        assertThat(q2t.getServerUri()).isEqualTo(URI.create("unix:unixSocketFile"));
        assertThat(q2t.isEnabled()).isTrue();
        assertThat(q2t.getApp()).isEqualTo(AppType.Q2T);

        ServerConfig p2p = results.get(1);
        assertThat(p2p.getCommunicationType()).isEqualTo(CommunicationType.GRPC);
        assertThat(p2p.getServerUri()).isEqualTo(URI.create("somehost:99"));
        assertThat(p2p.isEnabled()).isTrue();
        assertThat(p2p.getApp()).isEqualTo(AppType.P2P);
    }


    // TODO UNIX_SOCKET will be eliminated when the netty server will be able to cope with both Inet and Unix socket types
    @Test
    public void createConfigsFromDeprecatedServerConfigWithCommTypeUnixSocket() {

        DeprecatedServerConfig deprecatedServerConfig = new DeprecatedServerConfig();
        deprecatedServerConfig.setHostName("somehost");
        deprecatedServerConfig.setPort(99);
        deprecatedServerConfig.setCommunicationType(CommunicationType.REST);

        Path unixSocketFile = Paths.get("unixSocketFile");

        List<ServerConfig> results = DeprecatedServerConfig.from(deprecatedServerConfig, unixSocketFile);

        assertThat(results).hasSize(2);
        ServerConfig q2t = results.get(0);
        assertThat(q2t.getCommunicationType()).isEqualTo(CommunicationType.REST);
        assertThat(q2t.getServerUri()).isEqualTo(URI.create("unix:unixSocketFile"));
        assertThat(q2t.isEnabled()).isTrue();
        assertThat(q2t.getApp()).isEqualTo(AppType.Q2T);

        ServerConfig p2p = results.get(1);
        assertThat(p2p.getCommunicationType()).isEqualTo(CommunicationType.REST);
        assertThat(p2p.getServerUri()).isEqualTo(URI.create("somehost:99"));
        assertThat(p2p.isEnabled()).isTrue();
        assertThat(p2p.getApp()).isEqualTo(AppType.P2P);
    }
}
