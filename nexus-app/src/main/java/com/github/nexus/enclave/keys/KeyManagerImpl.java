package com.github.nexus.enclave.keys;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyManagerImpl implements KeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyManagerImpl.class);

    /**
     * A list of all pub/priv keys that are attached to this node
     */
    private final Set<KeyPair> ourKeys;

    private final NaclFacade nacl;

    private final String baseKeygenPath;

    public KeyManagerImpl(final String baseKeygenPath,
                          final NaclFacade nacl,
                          final List<String> publicKeys,
                          final List<JsonValue> privateKeys) {

        this.nacl = Objects.requireNonNull(nacl, "nacl is required");
        this.baseKeygenPath = Objects.requireNonNull(baseKeygenPath, "basepath is required");

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

    public KeyManagerImpl(final NaclFacade nacl, final Configuration configuration) {

        this(
            configuration.keygenBasePath(),
            nacl,
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
                () -> new RuntimeException("Private key " + privateKey + " not found when searching for public key")
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
                () -> new RuntimeException("Public key " + publicKey + " not found when searching for private key")
            );

        LOGGER.debug("Found private key {} for public key {}", privateKey, publicKey);

        return privateKey;
    }

    public KeyPair generateNewKeys(final String name) {
        LOGGER.info("Generating new public/private keypair with name " + name);

        final KeyPair generated = nacl.generateNewKeys();

        LOGGER.info("Generated new public/private keypair with name " + name);

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());
        final String privateKeyBase64 = Base64.getEncoder().encodeToString(generated.getPrivateKey().getKeyBytes());

        final Path workingDirectory = Paths.get(baseKeygenPath).toAbsolutePath();
        final Path publicKeyPath = workingDirectory.resolve(name + ".pub");
        final Path privateKeyPath = workingDirectory.resolve(name + ".key");

        final byte[] privateKeyJson = Json.createObjectBuilder()
            .add("type", "unlocked")
            .add("data", Json.createObjectBuilder()
                .add("bytes", privateKeyBase64)
            ).build().toString().getBytes(UTF_8);

        try {

            LOGGER.info("Attempting to write newly generated keys to file...");

            Files.write(publicKeyPath, publicKeyBase64.getBytes(UTF_8), StandardOpenOption.CREATE_NEW);
            Files.write(privateKeyPath, privateKeyJson, StandardOpenOption.CREATE_NEW);

            LOGGER.info("Successfully wrote newly generated keys to file");

        } catch (final IOException ex) {
            LOGGER.error("Unable to write the newly generated keys to file", ex);
            throw new RuntimeException(ex);
        }

        return generated;
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
