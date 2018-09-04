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
    private final Integer gRPCPort;

    @Valid
    @XmlElement
    @ValidSsl
    private final SslConfig sslConfig;

    @Valid
    @XmlElement
    private final InfluxConfig influxConfig;

    public ServerConfig(String hostName, Integer port, Integer gRPCPort, SslConfig sslConfig, InfluxConfig influxConfig) {
        this.hostName = hostName;
        this.port = port;
        this.gRPCPort = gRPCPort;
        this.sslConfig = sslConfig;
        this.influxConfig = influxConfig;
    }

    private static ServerConfig create() {
        return new ServerConfig(null, null, null, null, null);
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getgRPCPort() {
        return gRPCPort;
    }

    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public InfluxConfig getInfluxConfig() {
        return influxConfig;
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
