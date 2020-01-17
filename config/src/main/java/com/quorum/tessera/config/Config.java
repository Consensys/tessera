package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.*;
import com.quorum.tessera.config.constraints.groups.KeyValidationGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ValidEitherServerConfigsOrServer
public class Config extends ConfigItem {

    @XmlAttribute private String version = Version.getVersion();

    @NotNull
    @Valid
    @XmlElement(name = "jdbc", required = true)
    private JdbcConfig jdbcConfig;

    @Valid
    @ValidServerConfigs
    @XmlElement(name = "serverConfigs", required = true)
    private List<@Valid @ValidServerConfig ServerConfig> serverConfigs;

    @NotNull
    @Valid
    @XmlElement(name = "peer", required = true)
    private List<Peer> peers;

    @Valid
    @XmlElement(required = true)
    @ValidKeyConfiguration
    @MatchingKeyVaultConfigsForKeyData(groups = KeyValidationGroup.class)
    @NoDuplicateKeyVaultConfigs
    private KeyConfiguration keys;

    @NotNull
    @XmlElement(name = "alwaysSendTo")
    private List<@ValidBase64 String> alwaysSendTo = new ArrayList<>();

    @ValidPath(checkCanCreate = true)
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path unixSocketFile;

    @XmlAttribute private boolean useWhiteList;

    @XmlAttribute private boolean disablePeerDiscovery;

    @XmlElement private DeprecatedServerConfig server;

    @XmlElement private FeatureToggles features = new FeatureToggles();

    @XmlElement private EncryptorConfig encryptor;

    @Deprecated
    public Config(
            final JdbcConfig jdbcConfig,
            final List<ServerConfig> serverConfigs,
            final List<Peer> peers,
            final KeyConfiguration keyConfiguration,
            final List<String> alwaysSendTo,
            final Path unixSocketFile,
            final boolean useWhiteList,
            final boolean disablePeerDiscovery) {
        this.jdbcConfig = jdbcConfig;
        this.serverConfigs = serverConfigs;
        this.peers = peers;
        this.keys = keyConfiguration;
        this.alwaysSendTo = alwaysSendTo;
        this.unixSocketFile = unixSocketFile;
        this.useWhiteList = useWhiteList;
        this.disablePeerDiscovery = disablePeerDiscovery;
    }

    public Config() {}

    public JdbcConfig getJdbcConfig() {
        return this.jdbcConfig;
    }

    // TODO: Shouldn't need to laziely recalcuate on a getter
    public List<ServerConfig> getServerConfigs() {
        if (null != this.serverConfigs) {
            return this.serverConfigs;
        }
        return DeprecatedServerConfig.from(server, unixSocketFile);
    }

    public boolean isServerConfigsNull() {
        return null == this.serverConfigs;
    }

    @Deprecated
    public Path getUnixSocketFile() {
        return unixSocketFile;
    }

    public List<Peer> getPeers() {
        if (peers == null) {
            return null;
        }
        return Collections.unmodifiableList(peers);
    }

    public KeyConfiguration getKeys() {
        return this.keys;
    }

    public List<String> getAlwaysSendTo() {
        return this.alwaysSendTo;
    }

    public boolean isUseWhiteList() {
        return this.useWhiteList;
    }

    public boolean isDisablePeerDiscovery() {
        return disablePeerDiscovery;
    }

    public void addPeer(Peer peer) {
        if (peers == null) {
            this.peers = new ArrayList<>();
        }
        this.peers.add(peer);
    }

    public ServerConfig getP2PServerConfig() {
        // TODO need to revisit
        return getServerConfigs().stream()
                .filter(ServerConfig::isEnabled)
                .filter(sc -> sc.getApp() == AppType.P2P)
                .findFirst()
                .orElse(null);
    }

    @Deprecated
    public DeprecatedServerConfig getServer() {
        return server;
    }

    @Deprecated
    public void setServer(DeprecatedServerConfig server) {
        this.server = server;
    }

    public void setJdbcConfig(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    public void setServerConfigs(List<ServerConfig> serverConfigs) {
        this.serverConfigs = serverConfigs;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    public void setKeys(KeyConfiguration keys) {
        this.keys = keys;
    }

    public void setAlwaysSendTo(List<String> alwaysSendTo) {
        this.alwaysSendTo = alwaysSendTo;
    }

    @Deprecated
    public void setUnixSocketFile(Path unixSocketFile) {
        this.unixSocketFile = unixSocketFile;
    }

    public void setUseWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
    }

    public void setDisablePeerDiscovery(boolean disablePeerDiscovery) {
        this.disablePeerDiscovery = disablePeerDiscovery;
    }

    public String getVersion() {
        return version;
    }

    public FeatureToggles getFeatures() {
        return features;
    }

    public void setFeatures(final FeatureToggles features) {
        this.features = features;
    }

    public EncryptorConfig getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(EncryptorConfig encryptor) {
        this.encryptor = encryptor;
    }
}
