package com.quorum.tessera.admin;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.FeatureToggles;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.util.ConfigFileStore;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConfigServiceImpl implements ConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class);

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
        if (isDisablePeerDiscovery()) {
            LOGGER.warn(
                    "As peer discovery is being disabled, the use of peer whitelist restriction will be switched on."
                            + "This is to prevent unauthorized attempt to push transactions from unknown peers.");
            return true;
        }
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
