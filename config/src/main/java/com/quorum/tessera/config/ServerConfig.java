package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidServerAddress;
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

    @NotNull
    @XmlElement(required = true)
    private AppType app;

    @NotNull
    @XmlElement(required = true)
    private boolean enabled;

    @XmlElement private CommunicationType communicationType;

    @Valid @XmlElement @ValidSsl private SslConfig sslConfig;

    @Valid @XmlElement private InfluxConfig influxConfig;

    @ValidServerAddress(
            message = "Binding Address is invalid",
            isBindingAddress = true,
            supportedSchemes = {"http", "https"})
    @XmlElement
    private String bindingAddress;

    @ValidServerAddress(message = "Server Address is invalid")
    @NotNull
    @XmlElement
    private String serverAddress;

    @XmlElement(name = "cors")
    private CrossDomainConfig crossDomainConfig;

    public ServerConfig(
            final AppType app,
            final boolean enabled,
            final String serverAddress,
            final CommunicationType communicationType,
            final SslConfig sslConfig,
            final InfluxConfig influxConfig,
            final String bindingAddress) {
        this.app = app;
        this.enabled = enabled;
        this.serverAddress = serverAddress;
        this.communicationType = communicationType;
        this.sslConfig = sslConfig;
        this.influxConfig = influxConfig;
        this.bindingAddress = bindingAddress;
    }

    public ServerConfig() {}

    public String getBindingAddress() {
        return this.bindingAddress == null ? this.serverAddress : this.bindingAddress;
    }

    public URI getServerUri() {
        try {
            return URI.create(serverAddress);
        } catch (IllegalArgumentException ex) {
            throw new ConfigException(ex);
        }
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

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public boolean isUnixSocket() {
        return Objects.equals(getServerUri().getScheme(), "unix");
    }

    public CrossDomainConfig getCrossDomainConfig() {
        return crossDomainConfig;
    }

    public void setCrossDomainConfig(CrossDomainConfig crossDomainConfig) {
        this.crossDomainConfig = crossDomainConfig;
    }
}
