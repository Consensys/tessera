package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidSsl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class ServerConfig extends ConfigItem {

    //TODO validate that the server socket type and the communication type match the AppType
    @NotNull
    @XmlElement(required = true)
    private final AppType app;

    @NotNull
    @XmlElement(required = true)
    private final boolean enabled;

    @NotNull
    @XmlElement(required = true)
    @Valid
    private ServerSocket serverSocket;

    @XmlElement
    private final CommunicationType communicationType;

    @Valid
    @XmlElement
    @ValidSsl
    private final SslConfig sslConfig;

    @Valid
    @XmlElement
    private final InfluxConfig influxConfig;

    @XmlElement
    private final String bindingAddress;

    public ServerConfig(final AppType app,
                        final boolean enabled,
                        final ServerSocket serverSocket,
                        final CommunicationType communicationType,
                        final SslConfig sslConfig,
                        final InfluxConfig influxConfig,
                        final String bindingAddress) {
        this.app = app;
        this.enabled = enabled;
        this.serverSocket = serverSocket;
        this.communicationType = communicationType;
        this.sslConfig = sslConfig;
        this.influxConfig = influxConfig;
        this.bindingAddress = bindingAddress;
    }

    private static ServerConfig create() {
        return new ServerConfig(null, false, null, CommunicationType.REST, null, null, null);
    }

    public AppType getApp() {
        return app;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public CommunicationType getCommunicationType() {
        return communicationType;
    }

    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public InfluxConfig getInfluxConfig() {
        return influxConfig;
    }

    public String getBindingAddress() {
        return this.bindingAddress == null ? this.getServerUri().toString() : this.bindingAddress;
    }

    public URI getServerUri() {
        return serverSocket.getServerUri();
    }

    public boolean isSsl() {
        return Objects.nonNull(sslConfig) && sslConfig.getTls() == SslAuthenticationMode.STRICT;
    }

    public URI getBindingUri() {
        try {
            return new URI(this.getBindingAddress());
        } catch (URISyntaxException ex) {
            throw new ConfigException(ex);
        }
    }

}
