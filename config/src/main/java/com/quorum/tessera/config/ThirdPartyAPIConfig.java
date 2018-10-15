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
public class ThirdPartyAPIConfig {

    @NotNull
    @XmlElement(required = true)
    private final boolean enabled;

    @NotNull
    @XmlElement(required = true)
    private final String hostName;

    @NotNull
    @XmlElement
    private final Integer port;

    @Valid
    @XmlElement
    @ValidSsl
    private final SslConfig sslConfig;

    public ThirdPartyAPIConfig(@NotNull boolean enabled, @NotNull String hostName, @NotNull Integer port, @Valid SslConfig sslConfig) {
        this.enabled = enabled;
        this.hostName = hostName;
        this.port = port;
        this.sslConfig = sslConfig;
    }

    private static ThirdPartyAPIConfig create(){
        return new ThirdPartyAPIConfig(false, null, null, null);
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
}
