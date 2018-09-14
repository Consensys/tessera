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

    @NotNull
    @XmlElement(required = true)
    private final String hostName;

    @NotNull
    @XmlElement
    private final Integer port;

    @XmlElement
    private final Integer grpcPort;

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

    public ServerConfig(final String hostName,
                        final Integer port,
                        final Integer grpcPort,
                        final CommunicationType communicationType,
                        final SslConfig sslConfig,
                        final InfluxConfig influxConfig,
                        final String bindingAddress) {
        this.hostName = hostName;
        this.port = port;
        this.grpcPort = grpcPort;
        this.communicationType = communicationType;
        this.sslConfig = sslConfig;
        this.influxConfig = influxConfig;
        this.bindingAddress = bindingAddress;
    }

    private static ServerConfig create() {
        return new ServerConfig(null, null, null, CommunicationType.REST, null, null, null);
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getGrpcPort() {
        return grpcPort;
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
        try {
            return new URI(hostName + ":" + port);
        } catch (URISyntaxException ex) {
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

    public URI getGrpcUri() {
        try {
            return new URI(hostName + ":" + grpcPort);
        } catch (URISyntaxException ex) {
            throw new ConfigException(ex);
        }
    }

}
