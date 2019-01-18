package com.quorum.tessera.config;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.net.URISyntaxException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="INET")
public class InetServerSocket extends ServerSocket{

    @NotNull
    @XmlElement(required = true)
    private String hostName;

    @NotNull
    @XmlElement
    private Integer port;

    public InetServerSocket(@NotNull String hostName, @NotNull Integer port) {
        this.hostName = hostName;
        this.port = port;
    }

    public InetServerSocket(){
        this(null, null);
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

    public URI getServerUri(){
        try {
            return new URI(hostName + ":" + port);
        } catch (URISyntaxException ex) {
            throw new ConfigException(ex);
        }
    }

}
