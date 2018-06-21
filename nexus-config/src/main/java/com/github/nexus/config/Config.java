package com.github.nexus.config;

import java.nio.file.Path;
import java.util.List;

public interface Config {
    
    Path getKeyGenBasePath();
    
    <J extends JdbcConfig> J getJdbcConfig();
    
    <S extends ServerConfig> S getServerConfig();
    
    List<? extends PublicKey> getPublicKeys();
    
    List<? extends PrivateKey> getPrivateKeys();
    
    Path getUnixSocketFile();
    
    List<? extends Peer> getPeers();

}
