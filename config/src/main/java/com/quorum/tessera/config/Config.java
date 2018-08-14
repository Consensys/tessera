package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.KeyAdapter;
import com.quorum.tessera.config.adapters.KeyConfigurationAdapter;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidKeyConfiguration;
import com.quorum.tessera.config.constraints.ValidPath;
import com.quorum.tessera.nacl.Key;

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
public class Config extends ConfigItem {

    @NotNull
    @Valid
    @XmlElement(name = "jdbc", required = true)
    private final JdbcConfig jdbcConfig;

    @NotNull
    @Valid
    @XmlElement(name = "server", required = true)
    private final ServerConfig serverConfig;

    @NotNull
    @Size(min = 1, message = "At least 1 peer must be provided")
    @Valid
    @XmlElement(name = "peer", required = true)
    private final List<Peer> peers;

    @Valid
    @NotNull
    @XmlElement(required = true)
    @ValidKeyConfiguration
    @XmlJavaTypeAdapter(KeyConfigurationAdapter.class)
    private final KeyConfiguration keys;

    @NotNull
    @XmlElement(name = "alwaysSendTo", required = true)
    @XmlJavaTypeAdapter(KeyAdapter.class)
    private final List<Key> alwaysSendTo;

    @ValidPath(checkCanCreate = true)
    @NotNull
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path unixSocketFile;

    @XmlAttribute
    private final boolean useWhiteList;

    public Config(final JdbcConfig jdbcConfig,
                  final ServerConfig serverConfig,
                  final List<Peer> peers,
                  final KeyConfiguration keyConfiguration,
                  final List<Key> alwaysSendTo,
                  final Path unixSocketFile,
                  final boolean useWhiteList) {
        this.jdbcConfig = jdbcConfig;
        this.serverConfig = serverConfig;
        this.peers = peers;
        this.keys = keyConfiguration;
        this.alwaysSendTo = alwaysSendTo;
        this.unixSocketFile = unixSocketFile;
        this.useWhiteList = useWhiteList;
    }

    private static Config create() {
        return new Config();
    }

    private Config() {
        this(null, null, null, null, null, null, false);
    }

    public JdbcConfig getJdbcConfig() {
        return this.jdbcConfig;
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public Path getUnixSocketFile() {
        return this.unixSocketFile;
    }

    public List<Peer> getPeers() {
        return this.peers;
    }

    public KeyConfiguration getKeys() {
        return this.keys;
    }

    public List<Key> getAlwaysSendTo() {
        return this.alwaysSendTo;
    }

    public boolean isUseWhiteList() {
        return this.useWhiteList;
    }

}
