package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidSsl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class DeprecatedServerConfig extends ConfigItem {

    @NotNull
    @XmlElement(required = true)
    private String hostName;

    @NotNull
    @XmlElement
    private Integer port;

    @XmlElement
    private Integer grpcPort;

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

    public DeprecatedServerConfig() {
    }

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

    public Integer getGrpcPort() {
        return grpcPort;
    }

    public void setGrpcPort(Integer grpcPort) {
        this.grpcPort = grpcPort;
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

        ServerConfig q2tConfig = new ServerConfig();
        q2tConfig.setEnabled(true);
        q2tConfig.setApp(AppType.Q2T);
        q2tConfig.setCommunicationType(CommunicationType.REST);
        String uriValue = "unix:"+ String.valueOf(unixSocketFile);
        q2tConfig.setServerAddress(uriValue);

        ServerConfig p2pConfig = new ServerConfig();
        p2pConfig.setEnabled(true);
        p2pConfig.setApp(AppType.P2P);

        if (server.getCommunicationType() == CommunicationType.GRPC) {
            p2pConfig.setServerAddress(server.getHostName() +":"+ server.getGrpcPort());
            p2pConfig.setCommunicationType(CommunicationType.GRPC);
        } else {
            p2pConfig.setServerAddress(server.getHostName() +":"+ server.getPort());
            p2pConfig.setCommunicationType(CommunicationType.REST);
        }
        p2pConfig.setInfluxConfig(server.getInfluxConfig());
        p2pConfig.setSslConfig(server.getSslConfig());
        p2pConfig.setBindingAddress(server.getBindingAddress());

        List<ServerConfig> srvConfigs = new ArrayList<>();
        srvConfigs.add(q2tConfig);
        srvConfigs.add(p2pConfig);

        return srvConfigs;
    }

}
