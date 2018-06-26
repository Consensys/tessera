package com.github.nexus.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import javax.validation.Valid;
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
    
    @Valid
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
    
    public URI getServerUri() {
        try {
            return new URI(hostName +":"+ port);
        } catch (URISyntaxException ex) {
            throw new ConfigException(ex);
        }
    }
    
    public boolean isSsl() {
        return Objects.nonNull(sslConfig);
    }

}
