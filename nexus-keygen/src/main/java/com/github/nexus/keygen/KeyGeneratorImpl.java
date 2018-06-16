package com.github.nexus.keygen;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyGeneratorImpl implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyGeneratorImpl.class);

    private final Path basePath;

    private final NaclFacade nacl;

    private final KeyEncryptor keyEncryptor;

    public KeyGeneratorImpl(final NaclFacade nacl,
                            final Configuration configuration,
                            final KeyEncryptor keyEncryptor) {
        this.basePath = configuration.keygenBasePath();
        this.nacl = Objects.requireNonNull(nacl);
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
    }

    public KeyPair generateNewKeys(final String name, final String password) {
        LOGGER.info("Generating new public/private keypair with name " + name);

        final KeyPair generated = nacl.generateNewKeys();

        LOGGER.info("Generated new public/private keypair with name " + name);

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());
        final JsonObject encryptedKey = keyEncryptor.encryptPrivateKey(generated.getPrivateKey(), password);

        final String privateKeyJson = Json.createObjectBuilder()
            .add("type", "argon2sbox")
            .add("data", encryptedKey)
            .build()
            .toString();

        writeKeysToFile(name, publicKeyBase64, privateKeyJson);

        return generated;
    }

    @Override
    public KeyPair generateNewKeys(final String name) {
        LOGGER.info("Generating new public/private keypair with name " + name);

        final KeyPair generated = nacl.generateNewKeys();

        LOGGER.info("Generated new public/private keypair with name " + name);

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());
        final String privateKeyBase64 = Base64.getEncoder().encodeToString(generated.getPrivateKey().getKeyBytes());

        final String privateKeyJson = Json.createObjectBuilder()
            .add("type", "unlocked")
            .add("data", Json.createObjectBuilder()
                .add("bytes", privateKeyBase64)
            ).build().toString();

        writeKeysToFile(name, publicKeyBase64, privateKeyJson);

        return generated;
    }

    private void writeKeysToFile(final String name, final String publicKeyb64, final String privateKeyJson) {

        final Path publicKeyPath = basePath.resolve(name + ".pub");
        final Path privateKeyPath = basePath.resolve(name + ".key");

        try {

            LOGGER.info("Attempting to write newly generated keys to file...");

            Files.write(publicKeyPath, publicKeyb64.getBytes(UTF_8), StandardOpenOption.CREATE_NEW);
            Files.write(privateKeyPath, privateKeyJson.getBytes(UTF_8), StandardOpenOption.CREATE_NEW);

            LOGGER.info("Successfully wrote newly generated keys to file");

        } catch (final IOException ex) {
            LOGGER.error("Unable to write the newly generated keys to file", ex);
            throw new RuntimeException(ex);
        }

    }

}
