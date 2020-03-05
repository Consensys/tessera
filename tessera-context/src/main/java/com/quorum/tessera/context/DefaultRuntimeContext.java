package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.List;

class DefaultRuntimeContext implements RuntimeContext {

    private final List<KeyPair> keys;

    private final KeyEncryptor keyEncryptor;

    private final List<PublicKey> alwaysSendTo;

    private final List<URI> peers;

    private final Client p2pClient;

    private final boolean remoteKeyValidation;

    private final URI p2pServerUri;

    private final boolean disablePeerDiscovery;

    private final boolean useWhiteList;

    protected DefaultRuntimeContext(
            List<KeyPair> keys,
            KeyEncryptor keyEncryptor,
            List<PublicKey> alwaysSendTo,
            List<URI> peers,
            Client p2pClient,
            boolean remoteKeyValidation,
            URI p2pServerUri,
            boolean disablePeerDiscovery,
            boolean useWhiteList) {
        this.keys = List.copyOf(keys);
        this.keyEncryptor = keyEncryptor;
        this.alwaysSendTo = List.copyOf(alwaysSendTo);
        this.peers = List.copyOf(peers);
        this.p2pClient = p2pClient;
        this.remoteKeyValidation = remoteKeyValidation;
        this.p2pServerUri = p2pServerUri;
        this.disablePeerDiscovery = disablePeerDiscovery;
        this.useWhiteList = useWhiteList;
    }

    public List<KeyPair> getKeys() {
        return keys;
    }

    public KeyEncryptor getKeyEncryptor() {
        return keyEncryptor;
    }

    public List<PublicKey> getAlwaysSendTo() {
        return alwaysSendTo;
    }

    public List<URI> getPeers() {
        return peers;
    }

    public Client getP2pClient() {
        return p2pClient;
    }

    public boolean isRemoteKeyValidation() {
        return remoteKeyValidation;
    }

    public URI getP2pServerUri() {
        return p2pServerUri;
    }

    @Override
    public boolean isDisablePeerDiscovery() {
        return disablePeerDiscovery;
    }

    @Override
    public boolean isUseWhiteList() {
        return useWhiteList;
    }

    @Override
    public String toString() {
        return "DefaultRuntimeContext{"
                + "keys="
                + keys
                + ", keyEncryptor="
                + keyEncryptor
                + ", alwaysSendTo="
                + alwaysSendTo
                + ", peers="
                + peers
                + ", p2pClient="
                + p2pClient
                + ", remoteKeyValidation="
                + remoteKeyValidation
                + ", p2pServerUri="
                + p2pServerUri
                + ", disablePeerDiscovery="
                + disablePeerDiscovery
                + ", useWhiteList="
                + useWhiteList
                + '}';
    }
}
