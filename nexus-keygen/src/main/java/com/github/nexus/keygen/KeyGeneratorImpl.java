package com.github.nexus.keygen;


import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyGeneratorImpl implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyGeneratorImpl.class);

    private final Path basePath;

    private final NaclFacade nacl;

    private final KeyEncryptor keyEncryptor;

    public KeyGeneratorImpl(final NaclFacade nacl, final Configuration config, final KeyEncryptor keyEncryptor) {
        this.basePath = config.keygenBasePath();
        this.nacl = Objects.requireNonNull(nacl);
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
    }

    @Override
    public Pair<String, String> generateNewKeys(final String name, final String password) {
        LOGGER.debug("Generating new public/private keypair with name " + name);

        final KeyPair generated = nacl.generateNewKeys();

        LOGGER.debug("Generated new public/private keypair with name " + name);

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());
        final JsonObject encryptedKey = keyEncryptor.encryptPrivateKey(generated.getPrivateKey(), password);

        final String privateKeyJson = Json.createObjectBuilder()
            .add("type", "argon2sbox")
            .add("data", encryptedKey)
            .build()
            .toString();

        return new Pair<>(publicKeyBase64, privateKeyJson);
    }

    @Override
    public Pair<String, String> generateNewKeys(final String name) {
        LOGGER.debug("Generating new public/private keypair with name " + name);

        final KeyPair generated = nacl.generateNewKeys();

        LOGGER.debug("Generated new public/private keypair with name " + name);

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());
        final String privateKeyBase64 = Base64.getEncoder().encodeToString(generated.getPrivateKey().getKeyBytes());

        final String privateKeyJson = Json.createObjectBuilder()
            .add("type", "unlocked")
            .add("data", Json.createObjectBuilder()
                .add("bytes", privateKeyBase64)
            ).build().toString();

        return new Pair<>(publicKeyBase64, privateKeyJson);

    }

    private void writeKeysToFile(final String name, final String publicKeyb64, final String privateKeyJson) {

        final Path publicKeyPath = basePath.resolve(name + ".pub");
        final Path privateKeyPath = basePath.resolve(name + ".key");

        try {

            LOGGER.debug("Attempting to write newly generated keys to file...");

            Files.write(publicKeyPath, publicKeyb64.getBytes(UTF_8), StandardOpenOption.CREATE_NEW);
            Files.write(privateKeyPath, privateKeyJson.getBytes(UTF_8), StandardOpenOption.CREATE_NEW);

            LOGGER.debug("Successfully wrote newly generated keys to file");

        } catch (final IOException ex) {
            LOGGER.debug("Unable to write the newly generated keys to file", ex);
            throw new RuntimeException(ex);
        }

    }

    @Override
    public void promptForGeneration(final String name, final InputStream input) {
        System.out.println("Generating key for key " + name);
        System.out.println("Enter password for key (blank for no password):");

        final Scanner scanner = new Scanner(input).useDelimiter("\\s");
        final String password = scanner.next();

        final Pair<String, String> keypair;
        if(password.trim().isEmpty()) {
            keypair = this.generateNewKeys(name);
        } else {
            keypair = this.generateNewKeys(name, password);
        }

        System.out.println("Do you want to write the keys to file? (y/n): ");
        final String toFile = scanner.next();

        if("y".equalsIgnoreCase(toFile)) {
            writeKeysToFile(name, keypair.left, keypair.right);
        } else {
            System.out.println(keypair.left);
            System.out.println(keypair.right);
        }

    }

}
