package com.github.nexus.config;

import com.github.nexus.config.adapters.PathAdapter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class Config {

    @NotNull
    @Valid
    @XmlElement(name = "jdbc", required = true)
    private final JdbcConfig jdbcConfig;

    @NotNull
    @Valid
    @XmlElement(name = "server", required = true)
    private final ServerConfig serverConfig;

    @NotNull
    @Size(min = 1)
    @XmlElement(name = "peer", required = true)
    private final List<Peer> peers;

    @NotNull
    @Size(min = 1)
    @XmlElement(name = "keys", required = true)
    private final List<KeyData> keys;

    @NotNull
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
        this(null, null, null, null, null, false);
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
