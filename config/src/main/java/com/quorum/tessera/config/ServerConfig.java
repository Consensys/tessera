package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidSsl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServerConfig extends ConfigItem {

    //TODO validate that the server socket type and the communication type match the AppType
    @NotNull
    @XmlElement(required = true)
    private AppType app;

    @NotNull
    @XmlElement(required = true)
    private boolean enabled;

    @NotNull
    @XmlElement(required = true)
    @Valid
    private ServerSocket serverSocket;

    @XmlElement
    private CommunicationType communicationType;

    @Valid
    @XmlElement
    @ValidSsl
    private SslConfig sslConfig;

    @Valid
    @XmlElement
    private InfluxConfig influxConfig;

    @XmlElement
    private String bindingAddress;

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

    public ServerConfig(){}

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

    public AppType getApp() {
        return app;
    }

    public void setApp(AppType app) {
        this.app = app;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public CommunicationType getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(CommunicationType communicationType) {
        this.communicationType = communicationType;
    }

    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    public InfluxConfig getInfluxConfig() {
        return influxConfig;
    }

    public void setInfluxConfig(InfluxConfig influxConfig) {
        this.influxConfig = influxConfig;
    }

    public void setBindingAddress(String bindingAddress) {
        this.bindingAddress = bindingAddress;
    }
}
