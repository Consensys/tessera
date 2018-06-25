package com.github.nexus.config.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServerConfig", propOrder = {
    "port","sslConfig"
})
public class ServerConfig
    implements com.github.nexus.config.ServerConfig
{

    private int port;

    @XmlElement(required = false)
    private SslConfig sslConfig;

    @Override
    public int getPort() {
        return port;
    }


    public void setPort(int value) {
        this.port = value;
    }

    @Override
    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }
    

}
