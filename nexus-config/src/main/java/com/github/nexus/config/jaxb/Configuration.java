package com.github.nexus.config.jaxb;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.github.nexus.config.Config;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Configuration", propOrder = {
    "jdbcConfig",
    "serverConfig",
    "peers",
    "privateKey",
    "publicKey",
    "unixSocketFile"
})
public class Configuration implements Config {

    @XmlElement(name = "jdbc", required = true)
    private JdbcConfig jdbcConfig;

    @XmlElement(name = "server", required = true)
    private ServerConfig serverConfig;


    @XmlElement(name = "peer", required = true)
    private final List<Peer> peers = new ArrayList<>();

    @XmlElement(required = true)
    private PrivateKey privateKey;

    @XmlElement(required = true)
    private PublicKey publicKey;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path unixSocketFile;

    @Override
    public com.github.nexus.config.JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    @Override
    public com.github.nexus.config.ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public com.github.nexus.config.PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public com.github.nexus.config.PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public Path getUnixSocketFile() {
        return unixSocketFile;
    }

    @Override
    public List<com.github.nexus.config.Peer> getPeers() {
        return  peers.stream()
                .map(com.github.nexus.config.Peer.class::cast)
                .collect(Collectors.toList());
    }

   

}
