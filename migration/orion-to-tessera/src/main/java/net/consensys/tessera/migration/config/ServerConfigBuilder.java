package net.consensys.tessera.migration.config;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslConfig;

import java.util.Objects;
import java.util.Optional;

public class ServerConfigBuilder {

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
        Objects.requireNonNull(serverAddress, "Server Address is required");

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(type);

        serverConfig.setServerAddress(serverAddress);
        Optional.ofNullable(bindingAddress)
            .ifPresent(serverConfig::setBindingAddress);

        serverConfig.setCommunicationType(CommunicationType.REST);

        if (Objects.nonNull(sslConfig)) {
            serverConfig.setSslConfig(sslConfig);
        }

        return serverConfig;
    }

}
