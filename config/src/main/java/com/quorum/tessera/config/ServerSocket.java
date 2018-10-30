package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlSeeAlso;
import java.net.URI;

@XmlSeeAlso({UnixServerSocket.class, InetServerSocket.class})
public abstract class ServerSocket {
    public abstract URI getServerUri();
}
