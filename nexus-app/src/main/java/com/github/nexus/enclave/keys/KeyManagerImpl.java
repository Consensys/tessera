package com.github.nexus.enclave.keys;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.keys.model.KeyException;
import com.github.nexus.enclave.keys.model.KeyPair;
import com.github.nexus.encryption.NaclFacade;

import javax.json.Json;
import javax.json.JsonReader;
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

    /**
     * If no Sender is specified, this is the key that should be used
     */
    private final Key defaultSenderKey = null;

    /**
     * A list of all pub/priv keys that are attached to this node
     */
    private final Set<KeyPair> ourKeys;

    private final NaclFacade nacl;

    private final String baseKeygenPath;

    public KeyManagerImpl(final String baseKeygenPath, final NaclFacade nacl, final List<Path> publicKeyPaths, final List<Path> privateKeyPaths) {
        this(baseKeygenPath, nacl, null);

        if (publicKeyPaths.size() != privateKeyPaths.size()) {
            throw new RuntimeException("Key sizes don't match");
        }

        final Set<KeyPair> keys = IntStream
                .range(0, publicKeyPaths.size())
                .mapToObj(i -> loadKeypair(publicKeyPaths.get(i), privateKeyPaths.get(i)))
                .collect(Collectors.toSet());

        ourKeys.addAll(keys);
    }

    public KeyManagerImpl(final String baseKeygenPath, final NaclFacade nacl, final Collection<KeyPair> initialKeyset) {

        this.nacl = Objects.requireNonNull(nacl);
        this.baseKeygenPath = Objects.requireNonNull(baseKeygenPath);

        this.ourKeys = new HashSet<>();

        if (initialKeyset != null) {
            this.ourKeys.addAll(initialKeyset);
        }

    }

    @Override
    public Key getPublicKeyForPrivateKey(final Key privateKey) {
        return ourKeys
                .stream()
                .filter(keypair -> Objects.equals(keypair.getPrivateKey(), privateKey))
                .findFirst()
                .map(KeyPair::getPublicKey)
                .orElseThrow(() -> new RuntimeException("Public key not found!"));
    }

    @Override
    public Key getPrivateKeyForPublicKey(final Key publicKey) {
        return ourKeys
                .stream()
                .filter(keypair -> Objects.equals(keypair.getPublicKey(), publicKey))
                .findFirst()
                .map(KeyPair::getPrivateKey)
                .orElseThrow(() -> new RuntimeException("Private key not found!"));
    }

    public KeyPair generateNewKeys(final String name) {
        final KeyPair generated = nacl.generateNewKeys();

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

            Files.write(publicKeyPath, publicKeyBase64.getBytes(UTF_8), StandardOpenOption.CREATE_NEW);
            Files.write(privateKeyPath, privateKeyJson, StandardOpenOption.CREATE_NEW);

        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }

        return generated;
    }

    @Override
    public KeyPair loadKeypair(final Path publicKeyPath, final Path privateKeyPath) {

        try {

            final Key publicKey = loadPublicKey(publicKeyPath);

            final Key privateKey = loadPrivateKey(privateKeyPath, null);

            final KeyPair keyPair = new KeyPair(publicKey, privateKey);

            ourKeys.add(keyPair);

            return keyPair;

        } catch (final IOException ex) {
            throw new KeyException("Unable to load keypair", ex);
        }

    }

    private Key loadPublicKey(final Path publicKeyPath) throws IOException {
        final String publicKeyBase64 = new String(Files.readAllBytes(publicKeyPath), UTF_8);
        final byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

        return new Key(publicKeyBytes);
    }

    private Key loadPrivateKey(final Path privateKeyPath, final String password) throws IOException {
        final byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);
        final String jsonKey = new String(privateKeyBytes, UTF_8);

        final JsonReader reader = Json.createReader(new StringReader(jsonKey));
        final String keyBase64 = reader.readObject().getJsonObject("data").getString("bytes");

        final byte[] key = Base64.getDecoder().decode(keyBase64);

        return new Key(key);
    }


}
