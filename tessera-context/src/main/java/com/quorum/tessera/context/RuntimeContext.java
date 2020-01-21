package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.server.TesseraServer;

import java.net.URI;
import java.util.List;

public class RuntimeContext {

    private final List<KeyPair> keys;

    private final KeyEncryptor keyEncryptor;

    private final List<PublicKey> alwaysSendTo;

    private final List<TesseraServer> servers;

    private RuntimeContext(List<KeyPair> keys,
                           KeyEncryptor keyEncryptor,
                           List<PublicKey> alwaysSendTo,
                           List<TesseraServer> servers) {
        this.keys = keys;
        this.keyEncryptor = keyEncryptor;
        this.alwaysSendTo = alwaysSendTo;
        this.servers = servers;
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

    public List<TesseraServer> getServers() {
        return servers;
    }

    public static class Builder {

        private List<KeyPair> keys;

        private KeyEncryptor keyEncryptor;

        private List<PublicKey> alwaysSendTo;

        private List<TesseraServer> servers;

        private List<URI> peers;

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withKeys(List<KeyPair> keys) {
            this.keys = keys;
            return this;
        }

        public Builder withKeyEncryptor(KeyEncryptor keyEncryptor) {
            this.keyEncryptor = keyEncryptor;
            return this;
        }

        public Builder withPeers(List<URI> peers) {
            this.peers = peers;
            return this;
        }

        public Builder withServers(List<TesseraServer> servers) {
            this.servers = servers;
            return this;
        }

        public Builder withAlwaysSendTo(List<PublicKey> alwaysSendTo) {
            this.alwaysSendTo = alwaysSendTo;
            return this;
        }

        public RuntimeContext build() {
            return new RuntimeContext(keys,keyEncryptor,alwaysSendTo,servers);
        }

    }
}
