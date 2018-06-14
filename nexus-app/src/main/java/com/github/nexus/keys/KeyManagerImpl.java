package com.github.nexus.keys;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.keys.exception.KeyNotFoundException;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeyManagerImpl implements KeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyManagerImpl.class);

    /**
     * A list of all pub/priv keys that are attached to this node
     */
    private final Set<KeyPair> ourKeys;

    public KeyManagerImpl(final List<String> publicKeys, final List<JsonValue> privateKeys) {

        this.ourKeys = new HashSet<>();

        if (publicKeys.size() != privateKeys.size()) {
            LOGGER.error(
                "Provided public and private keys aren't one-to-one, {} public keys, {} private keys",
                publicKeys.size(), privateKeys.size()
            );
            throw new RuntimeException("Initial key list sizes don't match");
        }

        final Set<KeyPair> keys = IntStream
            .range(0, publicKeys.size())
            .mapToObj(i -> loadKeypair(publicKeys.get(i), privateKeys.get(i).asJsonObject()))
            .collect(Collectors.toSet());

        ourKeys.addAll(keys);

    }

    public KeyManagerImpl(final Configuration configuration) {

        this(
            configuration.publicKeys(),
            Json.createReader(new StringReader("[" + configuration.privateKeys() + "]")).readArray()
        );

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
    public KeyPair loadKeypair(final String publicKeyb64, final JsonObject privateKeyJson) {

        LOGGER.info("Attempting to load the public key at path {}", publicKeyb64);
        LOGGER.info("Attempting to load the private key at path {}", privateKeyJson);

        final Key publicKey = loadPublicKey(publicKeyb64);
        final Key privateKey = loadPrivateKey(privateKeyJson, null);

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

    private Key loadPublicKey(final String publicKeyBase64) {
        LOGGER.debug("Loading the public key {}", publicKeyBase64);

        final byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

        return new Key(publicKeyBytes);
    }

    private Key loadPrivateKey(final JsonObject privateKeyJson, final String password) {
        LOGGER.debug("Loading the private key at path {}", privateKeyJson);

        final String keyBase64 = privateKeyJson.getJsonObject("data").getString("bytes");

        final byte[] key = Base64.getDecoder().decode(keyBase64);

        LOGGER.debug("Private key {} loaded from path {} loaded", keyBase64, privateKeyJson);

        return new Key(key);
    }


}
