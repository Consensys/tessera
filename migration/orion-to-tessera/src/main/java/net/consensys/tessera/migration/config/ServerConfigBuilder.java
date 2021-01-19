package net.consensys.tessera.migration.config;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslConfig;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public class ServerConfigBuilder {

    private Integer serverPort;

    private String socketFile;

    private AppType type;

    private String serverAddress;

    private String bindingAddress;

    private SslConfig sslConfig;

    static ServerConfigBuilder create() {
        return new ServerConfigBuilder();
    }

    public ServerConfigBuilder withAppType(AppType type) {
        this.type = type;
        return this;
    }

    public ServerConfigBuilder withSocketFile(String socketFile) {
        this.socketFile = socketFile;
        return this;
    }

    public ServerConfigBuilder withServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
        return this;
    }

    public ServerConfigBuilder withBindingAddress(String bindingAddress) {
        this.bindingAddress = bindingAddress;
        return this;
    }

    public ServerConfigBuilder withSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
        return this;
    }

    public ServerConfig build() {

        Objects.requireNonNull(type);
        assert Objects.nonNull(socketFile) || Objects.nonNull(serverAddress)
                : "Either socketFile serveradress must be present";

        assert !(Objects.nonNull(socketFile) && Objects.nonNull(serverAddress))
                : "Both serverAddress and socket file cannot be specifed";

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(type);

        if (Objects.isNull(socketFile)) {
            URI serverUri = URI.create(serverAddress);
            if (Objects.nonNull(serverPort)) {
                assert serverUri.getPort() == serverPort : "Server URI port and configured server port domt match";
            }

            Optional.ofNullable(bindingAddress).ifPresent(serverConfig::setBindingAddress);
            serverConfig.setServerAddress(serverAddress);

        } else {
            serverConfig.setServerAddress(String.format("unix://%s", socketFile));
        }

        serverConfig.setCommunicationType(CommunicationType.REST);

        if (Objects.nonNull(sslConfig)) {
            serverConfig.setSslConfig(sslConfig);
        }

        return serverConfig;
    }

    public ServerConfigBuilder withServerPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }
}
