package com.quorum.tessera.admin;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.FeatureToggles;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.util.ConfigFileStore;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConfigServiceImpl implements ConfigService {

    private final Config config;

    private final Enclave enclave;

    private final ConfigFileStore configFileStore;

    public ConfigServiceImpl(Config initialConfig, Enclave enclave, ConfigFileStore configFileStore) {
        this.config = Objects.requireNonNull(initialConfig);
        this.enclave = Objects.requireNonNull(enclave);
        this.configFileStore = Objects.requireNonNull(configFileStore);
    }

    @Override
    public void addPeer(String url) {
        this.config.addPeer(new Peer(url));
        configFileStore.save(config);
    }

    @Override
    public List<Peer> getPeers() {
        return config.getPeers();
    }

    @Override
    public boolean isUseWhiteList() {
        return config.isUseWhiteList();
    }

    @Override
    public boolean isDisablePeerDiscovery() {
        return config.isDisablePeerDiscovery();
    }

    @Override
    public URI getServerUri() {
        return config.getP2PServerConfig().getServerUri();
    }

    @Override
    public Set<PublicKey> getPublicKeys() {
        return this.enclave.getPublicKeys();
    }

    @Override
    public FeatureToggles featureToggles() {
        return config.getFeatures();
    }
}
