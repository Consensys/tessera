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
public class ThirdPartyAPIConfig {

    @NotNull
    @XmlElement(required = true)
    private boolean enabled;

    @NotNull
    @XmlElement(required = true)
    private String hostName;

    @NotNull
    @XmlElement
    private Integer port;

    @Valid
    @XmlElement
    @ValidSsl
    private SslConfig sslConfig;

    public ThirdPartyAPIConfig(@NotNull boolean enabled, @NotNull String hostName, @NotNull Integer port, @Valid SslConfig sslConfig) {
        this.enabled = enabled;
        this.hostName = hostName;
        this.port = port;
        this.sslConfig = sslConfig;
    }

    public ThirdPartyAPIConfig() {
    }



    public boolean isEnabled() {
        return enabled;
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getPort() {
        return port;
    }

    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public URI getServerUri() {
        try {
            return new URI(hostName + ":" + port);
        } catch (URISyntaxException ex) {
            throw new ConfigException(ex);
        }
    }

    public boolean isSsl() {
        return Objects.nonNull(sslConfig) && sslConfig.getTls() == SslAuthenticationMode.STRICT;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }
    
    
}
