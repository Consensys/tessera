package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.config.util.PasswordReader;
import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileKeyGenerator implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileKeyGenerator.class);

    private static final String EMPTY_FILENAME = "";

    private final NaclFacade nacl;

    private final KeyEncryptor keyEncryptor;

    private final PasswordReader passwordReader;

    public FileKeyGenerator(final NaclFacade nacl, final KeyEncryptor keyEncryptor, final PasswordReader passwordReader) {
        this.nacl = Objects.requireNonNull(nacl);
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
        this.passwordReader = Objects.requireNonNull(passwordReader);
    }

    @Override
    public FilesystemKeyPair generate(final String filename, final ArgonOptions encryptionOptions) {

        final String password = this.passwordReader.requestUserPassword();

        final KeyPair generated = this.nacl.generateNewKeys();

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());

        final KeyData finalKeys;

        if (!password.isEmpty()) {

            final PrivateKeyData encryptedPrivateKey = this.keyEncryptor.encryptPrivateKey(
                generated.getPrivateKey(), password, encryptionOptions
            );

            finalKeys = new KeyData(
                new KeyDataConfig(
                    new PrivateKeyData(
                        null,
                        encryptedPrivateKey.getSnonce(),
                        encryptedPrivateKey.getAsalt(),
                        encryptedPrivateKey.getSbox(),
                        encryptedPrivateKey.getArgonOptions(),
                        null
                    ),
                    PrivateKeyType.LOCKED
                ),
                generated.getPrivateKey().toString(),
                publicKeyBase64,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

            LOGGER.info("Newly generated private key has been encrypted");

        } else {
            
            String keyData = Base64.getEncoder().encodeToString(generated.getPrivateKey().getKeyBytes());
            
            finalKeys = new KeyData(
                new KeyDataConfig(
                    new PrivateKeyData(keyData, null, null, null, null, null),
                    PrivateKeyType.UNLOCKED
                ),
                generated.getPrivateKey().toString(),
                publicKeyBase64,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

        }

        final String privateKeyJson = JaxbUtil.marshalToString(finalKeys.getConfig());

        final Path resolvedPath = Paths.get(filename).toAbsolutePath();
        final Path parentPath;

        if(EMPTY_FILENAME.equals(filename)) {
            parentPath = resolvedPath;
        } else {
            parentPath = resolvedPath.getParent();
        }

        final Path publicKeyPath = parentPath.resolve(filename + ".pub");
        final Path privateKeyPath = parentPath.resolve(filename + ".key");

        IOCallback.execute(() -> Files.write(publicKeyPath, publicKeyBase64.getBytes(UTF_8), StandardOpenOption.CREATE_NEW));
        IOCallback.execute(() -> Files.write(privateKeyPath, privateKeyJson.getBytes(UTF_8), StandardOpenOption.CREATE_NEW));

        LOGGER.info("Saved public key to {}", publicKeyPath.toAbsolutePath().toString());
        LOGGER.info("Saved private key to {}", privateKeyPath.toAbsolutePath().toString());

        final FilesystemKeyPair keyPair = new FilesystemKeyPair(publicKeyPath, privateKeyPath);

        keyPair.withPassword(password);

        return keyPair;
    }

}
