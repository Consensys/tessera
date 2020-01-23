package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidSsl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.quorum.tessera.config.AppType.P2P;
import static com.quorum.tessera.config.AppType.Q2T;
import static com.quorum.tessera.config.CommunicationType.REST;

@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class DeprecatedServerConfig extends ConfigItem {

    @NotNull
    @XmlElement(required = true)
    private String hostName;

    @NotNull @XmlElement private Integer port;

    @XmlElement private CommunicationType communicationType;

    @Valid @XmlElement @ValidSsl private SslConfig sslConfig;

    @Valid @XmlElement private InfluxConfig influxConfig;

    @XmlElement private String bindingAddress;

    public DeprecatedServerConfig() {}

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public String getBindingAddress() {
        if (bindingAddress == null) {
            this.bindingAddress = hostName + ":" + port;
        }
        return bindingAddress;
    }

    public void setBindingAddress(String bindingAddress) {
        this.bindingAddress = bindingAddress;
    }

    public static List<ServerConfig> from(DeprecatedServerConfig server, Path unixSocketFile) {
        if (null == server) {
            return Collections.emptyList();
        }

        final ServerConfig q2tConfig =
                new ServerConfig(Q2T, "unix:" + unixSocketFile, REST, null, server.getInfluxConfig(), null);

        final Integer port = server.getPort();

        final ServerConfig p2pConfig =
                new ServerConfig(
                        P2P,
                        server.getHostName() + ":" + port,
                        server.getCommunicationType(),
                        server.getSslConfig(),
                        server.getInfluxConfig(),
                        server.getBindingAddress());

        return Arrays.asList(q2tConfig, p2pConfig);
    }
}
