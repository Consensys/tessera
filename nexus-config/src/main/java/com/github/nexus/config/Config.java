package com.github.nexus.config;

import java.nio.file.Path;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class Config {

    @XmlElement(name = "jdbc", required = true)
    private final JdbcConfig jdbcConfig;

    @XmlElement(name = "server", required = true)
    private final ServerConfig serverConfig;

    @XmlElement(name = "peer", required = true)
    private final List<Peer> peers;

    @XmlElement(name = "keys", required = true)
    private final List<KeyData> keys;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path unixSocketFile;

    @XmlAttribute
    private final boolean useWhiteList;

    public Config(
            JdbcConfig jdbcConfig,
            ServerConfig serverConfig,
            List<Peer> peers,
            List<KeyData> keys,
            Path unixSocketFile,
            boolean useWhiteList) {
        this.jdbcConfig = jdbcConfig;
        this.serverConfig = serverConfig;
        this.peers = peers;
        this.keys = keys;
        this.unixSocketFile = unixSocketFile;
        this.useWhiteList = useWhiteList;
    }

    private static Config create() {
        return new Config();
    }

    private Config() {
        this(null, null, null, null,null, false);
    }


    public JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public Path getUnixSocketFile() {
        return unixSocketFile;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public List<KeyData> getKeys() {
        return keys;
    }

    public boolean isUseWhiteList() {
        return useWhiteList;
    }

}
