package com.github.nexus.config.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServerConfig", propOrder = {
    "port"
})
public class ServerConfig
    implements com.github.nexus.config.ServerConfig
{

    private int port;


    @Override
    public int getPort() {
        return port;
    }


    public void setPort(int value) {
        this.port = value;
    }

}
