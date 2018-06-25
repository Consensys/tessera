package com.github.nexus.config.api;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.github.nexus.config.Config;
import com.github.nexus.config.jaxb.PathAdapter;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Configuration", propOrder = {
    "jdbcConfig",
    "serverConfig",
    "keyGenConfig",
    "peers",
    "privateKey",
    "publicKey",
    "unixSocketFile"
})
public class Configuration implements Config {

    @XmlElement(name = "jdbc", required = true)
    protected JdbcConfig jdbcConfig;

    @XmlElement(name = "server", required = true)
    protected ServerConfig serverConfig;

    @XmlElement(name = "keyGen", required = true)
    protected KeyGenConfig keyGenConfig;

    @XmlElement(name = "peer", required = true)
    protected List<Peer> peers;

    @XmlElement(required = true)
    protected PrivateKey privateKey;

    @XmlElement(required = true)
    protected PublicKey publicKey;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    protected Path unixSocketFile;

    @Override
    public com.github.nexus.config.JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public void setJdbcConfig(com.github.nexus.config.JdbcConfig value) {
        this.jdbcConfig = (JdbcConfig) value;
    }

    @Override
    public com.github.nexus.config.ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(com.github.nexus.config.ServerConfig value) {
        this.serverConfig = (ServerConfig) value;
    }

    @Override
    public com.github.nexus.config.KeyGenConfig getKeyGenConfig() {
        return keyGenConfig;
    }

    public void setKeyGenConfig(com.github.nexus.config.KeyGenConfig value) {
        this.keyGenConfig = (KeyGenConfig) value;
    }

    @Override
    public List<com.github.nexus.config.Peer> getPeers() {
        if (peers == null) {
            peers = new ArrayList<>();
        }
        return peers.stream()
                .map(com.github.nexus.config.Peer.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public com.github.nexus.config.PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(com.github.nexus.config.PrivateKey value) {
        this.privateKey = (PrivateKey) value;
    }

    @Override
    public com.github.nexus.config.PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey value) {
        this.publicKey = value;
    }

    @Override
    public Path getUnixSocketFile() {
        return unixSocketFile;
    }

    public void setUnixSocketFile(Path value) {
        this.unixSocketFile = value;
    }

}
