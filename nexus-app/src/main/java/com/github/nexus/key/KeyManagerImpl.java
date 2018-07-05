package com.github.nexus.key;

import com.github.nexus.config.Config;
import com.github.nexus.key.exception.KeyNotFoundException;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyManagerImpl implements KeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyManagerImpl.class);

    /**
     * A list of all pub/priv keys that are attached to this node
     */
    private final Set<KeyPair> ourKeys;

    private final KeyPair defaultKeys;

    public KeyManagerImpl(final Config configuration) {
        this.ourKeys = configuration
            .getKeys()
            .stream()
            .map(kd -> new KeyPair(
                    new Key(Base64.getDecoder().decode(kd.getPublicKey())),
                    new Key(Base64.getDecoder().decode(kd.getPrivateKey()))
                )
            ).collect(Collectors.toSet());

        this.defaultKeys = ourKeys.iterator().next();
    }

    @Override
    public Key getPublicKeyForPrivateKey(final Key privateKey) {
        LOGGER.debug("Attempting to find public key for the private key {}", privateKey);

        final Key publicKey = ourKeys
            .stream()
            .filter(keypair -> Objects.equals(keypair.getPrivateKey(), privateKey))
            .findFirst()
            .map(KeyPair::getPublicKey)
            .orElseThrow(
                () -> new KeyNotFoundException("Private key " + privateKey + " not found when searching for public key")
            );

        LOGGER.debug("Found public key {} for private key {}", publicKey, privateKey);

        return publicKey;
    }

    @Override
    public Key getPrivateKeyForPublicKey(final Key publicKey) {
        LOGGER.debug("Attempting to find private key for the public key {}", publicKey);

        final Key privateKey = ourKeys
            .stream()
            .filter(keypair -> Objects.equals(keypair.getPublicKey(), publicKey))
            .findFirst()
            .map(KeyPair::getPrivateKey)
            .orElseThrow(
                () -> new KeyNotFoundException("Public key " + publicKey + " not found when searching for private key")
            );

        LOGGER.debug("Found private key {} for public key {}", privateKey, publicKey);

        return privateKey;
    }

    @Override
    public Set<Key> getPublicKeys() {
        return ourKeys
            .stream()
            .map(KeyPair::getPublicKey)
            .collect(Collectors.toSet());
    }

    @Override
    public Key defaultPublicKey() {
        return defaultKeys.getPublicKey();
    }

}
