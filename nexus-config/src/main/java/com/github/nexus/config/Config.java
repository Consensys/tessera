package com.github.nexus.config;

import java.nio.file.Path;
import java.util.List;

public interface Config {

    JdbcConfig getJdbcConfig();

    ServerConfig getServerConfig();

    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    Path getUnixSocketFile();

    List<Peer> getPeers();


}
