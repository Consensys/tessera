package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidSsl;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

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



    
    public static List<ServerConfig> from(DeprecatedServerConfig serverConfig,Optional<Path> unixSocketFile) {
        
        List<ServerConfig> serverConfigs = new ArrayList<>(AppType.values().length);

        for (AppType app : AppType.values()) {

            ServerConfig newConfig = new ServerConfig();
            newConfig.setEnabled(true);

            if (serverConfig.getGrpcPort() != null) {
                newConfig.setCommunicationType(CommunicationType.GRPC);
                newConfig.setServerSocket(new InetServerSocket(serverConfig.getHostName(), serverConfig.getGrpcPort()));
            } else if (unixSocketFile.isPresent()) {
                newConfig.setCommunicationType(CommunicationType.UNIX_SOCKET);
                newConfig.setServerSocket(new UnixServerSocket(unixSocketFile.get().toString()));
            } else {
                newConfig.setCommunicationType(CommunicationType.REST);
                newConfig.setServerSocket(new InetServerSocket(serverConfig.getHostName(), serverConfig.getPort()));
            }

            newConfig.setInfluxConfig(serverConfig.getInfluxConfig());
            newConfig.setSslConfig(serverConfig.getSslConfig());
            newConfig.setApp(app);
            newConfig.setBindingAddress(serverConfig.getBindingAddress());

            serverConfigs.add(newConfig);
        }
        
        return Collections.unmodifiableList(serverConfigs);
    }

}
