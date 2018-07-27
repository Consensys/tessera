package com.quorum.tessera.key;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.key.exception.KeyNotFoundException;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.KeyPair;
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
    private final Set<KeyPair> localKeys;

    private final KeyPair defaultKeys;

    public KeyManagerImpl(final Config configuration) {
        this.localKeys = configuration
            .getKeys()
            .getKeyData()
            .stream()
            .map(kd -> new KeyPair(
                    new Key(Base64.getDecoder().decode(kd.getPublicKey())),
                    new Key(Base64.getDecoder().decode(kd.getPrivateKey()))
                )
            ).collect(Collectors.toSet());

        this.defaultKeys = localKeys.iterator().next();
    }

    @Override
    public Key getPublicKeyForPrivateKey(final Key privateKey) {
        LOGGER.debug("Attempting to find public key for the private key {}", privateKey);

        final Key publicKey = localKeys
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

        final Key privateKey = localKeys
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
        return localKeys
            .stream()
            .map(KeyPair::getPublicKey)
            .collect(Collectors.toSet());
    }

    @Override
    public Key defaultPublicKey() {
        return defaultKeys.getPublicKey();
    }

}
