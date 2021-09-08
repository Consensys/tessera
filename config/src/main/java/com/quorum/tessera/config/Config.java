package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ValidServerConfigs
@HasKeysOrRemoteEnclave
public class Config extends ConfigItem {

  @XmlAttribute private String version = Version.getVersion();

  @NotNull
  @Valid
  @XmlElement(name = "jdbc", required = true)
  private JdbcConfig jdbcConfig;

  @NotNull
  @Valid
  @Size(min = 1)
  @XmlElement(name = "serverConfigs", required = true)
  private List<@Valid @ValidServerConfig ServerConfig> serverConfigs = new ArrayList<>();

  @NotNull
  @Valid
  @XmlElement(name = "peer", required = true)
  private List<Peer> peers;

  @Valid
  @XmlElement(required = true)
  @ValidKeyConfiguration
  @MatchingKeyVaultConfigsForKeyData
  @NoDuplicateKeyVaultConfigs
  private KeyConfiguration keys;

  @NotNull
  @XmlElement(name = "alwaysSendTo")
  private List<@ValidBase64 String> alwaysSendTo = new ArrayList<>();

  @XmlAttribute private boolean useWhiteList;

  @XmlAttribute private boolean disablePeerDiscovery;

  @XmlAttribute private boolean bootstrapNode;

  @XmlElement private FeatureToggles features = new FeatureToggles();

  @XmlElement private EncryptorConfig encryptor;

  @XmlTransient private boolean recoveryMode;

  @XmlElement(name = "mode")
  private ClientMode clientMode = ClientMode.TESSERA;

  @XmlElement private List<ResidentGroup> residentGroups;

  @Deprecated
  public Config(
      final JdbcConfig jdbcConfig,
      final List<ServerConfig> serverConfigs,
      final List<Peer> peers,
      final KeyConfiguration keyConfiguration,
      final List<String> alwaysSendTo,
      final boolean useWhiteList,
      final boolean disablePeerDiscovery) {
    this.jdbcConfig = jdbcConfig;
    this.serverConfigs = serverConfigs;
    this.peers = peers;
    this.keys = keyConfiguration;
    this.alwaysSendTo = alwaysSendTo;
    this.useWhiteList = useWhiteList;
    this.disablePeerDiscovery = disablePeerDiscovery;
  }

  public Config() {}

  public JdbcConfig getJdbcConfig() {
    return this.jdbcConfig;
  }

  public List<ServerConfig> getServerConfigs() {
    return this.serverConfigs;
  }

  public boolean isServerConfigsNull() {
    return null == this.serverConfigs;
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

  public boolean isBootstrapNode() {
    return this.bootstrapNode;
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
        .filter(sc -> sc.getApp() == AppType.P2P)
        .findFirst()
        .orElse(null);
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

  public void setUseWhiteList(boolean useWhiteList) {
    this.useWhiteList = useWhiteList;
  }

  public void setDisablePeerDiscovery(boolean disablePeerDiscovery) {
    this.disablePeerDiscovery = disablePeerDiscovery;
  }

  public void setBootstrapNode(boolean bootstrapNode) {
    this.bootstrapNode = bootstrapNode;
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

  public boolean isRecoveryMode() {
    return recoveryMode;
  }

  public void setRecoveryMode(boolean recoveryMode) {
    this.recoveryMode = recoveryMode;
  }

  public ClientMode getClientMode() {
    return clientMode;
  }

  public void setClientMode(ClientMode clientMode) {
    this.clientMode = clientMode;
  }

  public List<ResidentGroup> getResidentGroups() {
    return residentGroups;
  }

  public void setResidentGroups(List<ResidentGroup> residentGroups) {
    this.residentGroups = residentGroups;
  }
}
