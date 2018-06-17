package com.github.nexus.key;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.configuration.model.KeyData;
import com.github.nexus.key.exception.KeyNotFoundException;
import com.github.nexus.keygen.KeyEncryptor;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.util.*;
import java.util.stream.Collectors;

public class KeyManagerImpl implements KeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyManagerImpl.class);

    /**
     * A list of all pub/priv keys that are attached to this node
     */
    private final Set<KeyPair> ourKeys;

    private final KeyEncryptor keyEncryptor;

    public KeyManagerImpl(final KeyEncryptor keyEncryptor, final List<KeyData> keys) {
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);

        this.ourKeys = new HashSet<>();
        keys.forEach(this::loadKeypair);

    }

    public KeyManagerImpl(final KeyEncryptor keyEncryptor, final Configuration configuration) {
        this(keyEncryptor, configuration.keyData());
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
    public KeyPair loadKeypair(final KeyData data) {

        LOGGER.info("Attempting to load the public key {}", data.getPublicKey());
        LOGGER.info("Attempting to load the private key {}", data.getPrivateKey());

        final Key publicKey = new Key(
            Base64.getDecoder().decode(data.getPublicKey())
        );
        final Key privateKey = loadPrivateKey(data.getPrivateKey(), data.getPassword());

        final KeyPair keyPair = new KeyPair(publicKey, privateKey);

        ourKeys.add(keyPair);

        return keyPair;

    }

    @Override
    public Set<Key> getPublicKeys() {
        return ourKeys
            .stream()
            .map(KeyPair::getPublicKey)
            .collect(Collectors.toSet());
    }

    private Key loadPrivateKey(final JsonObject privateKeyJson, final String password) {
        LOGGER.debug("Loading the private key at path {}", privateKeyJson);

        if("unlocked".equals(privateKeyJson.getString("type"))) {
            final String keyBase64 = privateKeyJson.getJsonObject("data").getString("bytes");
            final byte[] key = Base64.getDecoder().decode(keyBase64);
            LOGGER.debug("Private key {} loaded from path {} loaded", keyBase64, privateKeyJson);
            return new Key(key);
        } else {
            return keyEncryptor.decryptPrivateKey(privateKeyJson.getJsonObject("data"), password);
        }

    }


}
