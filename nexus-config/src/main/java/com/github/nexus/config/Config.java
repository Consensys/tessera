package com.github.nexus.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public interface Config {

    JdbcConfig getJdbcConfig();

    ServerConfig getServerConfig();


    Path getUnixSocketFile();

    List<Peer> getPeers();
    
    default boolean hasSslConfig() {
        return Objects.nonNull(getServerConfig());
    }
    
    List<KeyData> getKeys();
    
    
    boolean isUseWhiteList();
    

}
