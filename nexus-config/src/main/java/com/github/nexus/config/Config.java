package com.github.nexus.config;

import java.nio.file.Path;
import java.util.List;

public interface Config {
    
    Path getKeyGenBasePath();
    
    JdbcConfig getJdbcConfig();
    
    ServerConfig getServerConfig();
    
    List<PublicKey> getPublicKeys();
    
    List<PrivateKey> getPrivateKeys();
    
    Path getUnixSocketFile();
    
    List<Peer> getPeers();

}
