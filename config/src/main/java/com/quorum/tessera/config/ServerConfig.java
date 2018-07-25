package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidSsl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class ServerConfig extends ConfigItem {

    @NotNull
    @XmlElement(required = false, defaultValue = "0.0.0.0")
    private final String hostName;

    @NotNull
    @XmlElement
    private final Integer port;

    @Valid
    @XmlElement(required = false)
    @ValidSsl
    private final SslConfig sslConfig;

    public ServerConfig(String hostName, Integer port, SslConfig sslConfig) {
        this.hostName = Optional.ofNullable(hostName).orElse("0.0.0.0");
        this.port = port;
        this.sslConfig = sslConfig;
    }

    private static ServerConfig create() {
        return new ServerConfig(null, null, null);
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
