package com.github.nexus.config;

import java.nio.file.Path;
import java.util.List;

public interface Config {
    
    <J extends JdbcConfig> J getJdbcConfig();
    
    <S extends ServerConfig> S getServerConfig();

    <R extends PrivateKey> R getPrivateKey();
    
    <P extends PublicKey> P getPublicKey();
    
    Path getUnixSocketFile();
    
    List<? extends Peer> getPeers();

    <K extends KeyGenConfig> K  getKeyGenConfig();
    
}
