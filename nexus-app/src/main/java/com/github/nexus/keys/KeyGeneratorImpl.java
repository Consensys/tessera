package com.github.nexus.keys;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
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

    public KeyGeneratorImpl(final NaclFacade nacl, final Configuration configuration) {
        this.basePath = configuration.keygenBasePath();
        this.nacl = Objects.requireNonNull(nacl);
    }

    @Override
    public KeyPair generateNewKeys(final String name) {
        LOGGER.info("Generating new public/private keypair with name " + name);

        final KeyPair generated = nacl.generateNewKeys();

        LOGGER.info("Generated new public/private keypair with name " + name);

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());
        final String privateKeyBase64 = Base64.getEncoder().encodeToString(generated.getPrivateKey().getKeyBytes());

        final Path publicKeyPath = basePath.resolve(name + ".pub");
        final Path privateKeyPath = basePath.resolve(name + ".key");

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
}
