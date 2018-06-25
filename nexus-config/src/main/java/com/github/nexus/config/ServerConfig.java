package com.github.nexus.config;

import java.net.URI;
import java.net.URISyntaxException;


public interface ServerConfig {

    int getPort();

    SslConfig getSslConfig();

    String getHostName();

    default URI getServerUri() {
        try {
            return new URI(getHostName() + ":"+ getPort());
        } catch (URISyntaxException ex) {
            throw new ConfigException(ex);
        }
    }
    
}
