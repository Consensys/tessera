package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidSsl;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class DeprecatedServerConfig extends ConfigItem {
    
    @NotNull
    @XmlElement(required = true)
    private  String hostName;

    @NotNull
    @XmlElement
    private  Integer port;

    @XmlElement
    private  Integer grpcPort;

    @XmlElement
    private  CommunicationType communicationType;

    @Valid
    @XmlElement
    @ValidSsl
    private  SslConfig sslConfig;

    @Valid
    @XmlElement
    private  InfluxConfig influxConfig;

    @XmlElement
    private  String bindingAddress;
    
    public static DeprecatedServerConfig create() {
        return new DeprecatedServerConfig();
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
        if(bindingAddress == null) {
            this.bindingAddress = hostName + ":"+ port;
        }
        return bindingAddress;
    }

    public void setBindingAddress(String bindingAddress) {
        this.bindingAddress = bindingAddress;
    }



}
