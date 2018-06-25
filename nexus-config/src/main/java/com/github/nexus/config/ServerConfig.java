package com.github.nexus.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class ServerConfig {
    
    @XmlElement(required = false,defaultValue = "0.0.0.0")
    private final String hostName;

    private final int port;

    @XmlElement(required = false)
    private final SslConfig sslConfig;

    public ServerConfig(String hostName, int port, SslConfig sslConfig) {
        this.hostName = hostName;
        this.port = port;
        this.sslConfig = sslConfig;
    }

    private static ServerConfig create() {
        return new ServerConfig(null,-1,null);
    }
 
    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public SslConfig getSslConfig() {
        return sslConfig;
    }

}
