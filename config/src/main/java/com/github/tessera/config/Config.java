package com.github.tessera.config;

import com.github.tessera.config.adapters.KeyDataAdapter;
import com.github.tessera.config.adapters.PathAdapter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

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

    @Valid
    @NotNull
    @Size(min = 1)
    @XmlElements({
        @XmlElement(type = KeyData.class)
    })
    @XmlElement
    @XmlJavaTypeAdapter(value = KeyDataAdapter.class)
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.jdbcConfig);
        hash = 47 * hash + Objects.hashCode(this.serverConfig);
        hash = 47 * hash + Objects.hashCode(this.peers);
        hash = 47 * hash + Objects.hashCode(this.keys);
        hash = 47 * hash + Objects.hashCode(this.unixSocketFile);
        hash = 47 * hash + Objects.hashCode(Boolean.valueOf(useWhiteList));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Config other = (Config) obj;
        if (this.useWhiteList != other.useWhiteList) {
            return false;
        }
        if (!Objects.equals(this.jdbcConfig, other.jdbcConfig)) {
            return false;
        }
        if (!Objects.equals(this.serverConfig, other.serverConfig)) {
            return false;
        }
        if (!Objects.equals(this.peers, other.peers)) {
            return false;
        }
        if (!Objects.equals(this.keys, other.keys)) {
            return false;
        }
        if (!Objects.equals(this.unixSocketFile, other.unixSocketFile)) {
            return false;
        }
        return true;
    }

}
