package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.config.util.PasswordReader;
import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.nacl.KeyPair;
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
    public KeyData generate(final String filename, final ArgonOptions encryptionOptions) {

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
                        generated.getPrivateKey().toString(),
                        encryptedPrivateKey.getSnonce(),
                        encryptedPrivateKey.getAsalt(),
                        encryptedPrivateKey.getSbox(),
                        new ArgonOptions(
                            encryptedPrivateKey.getArgonOptions().getAlgorithm(),
                            encryptedPrivateKey.getArgonOptions().getIterations(),
                            encryptedPrivateKey.getArgonOptions().getMemory(),
                            encryptedPrivateKey.getArgonOptions().getParallelism()
                        ),
                        password
                    ),
                    PrivateKeyType.LOCKED
                ),
                generated.getPrivateKey().toString(),
                publicKeyBase64,
                null,
                null,
                null,
                null
            );

            LOGGER.info("Newly generated private key has been encrypted");

        } else {

            finalKeys = new KeyData(
                new KeyDataConfig(
                    new PrivateKeyData(generated.getPrivateKey().toString(), null, null, null, null, null),
                    PrivateKeyType.UNLOCKED
                ),
                generated.getPrivateKey().toString(),
                publicKeyBase64,
                null,
                null,
                null,
                null
            );

        }

        final String privateKeyJson = this.privateKeyToJson(finalKeys);

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

        return finalKeys;
    }

    private String privateKeyToJson(final KeyData keyData) {

        final KeyDataConfig privateKey;

        if (PrivateKeyType.LOCKED.equals(keyData.getConfig().getType())) {

            privateKey = new KeyDataConfig(
                new PrivateKeyData(
                    null,
                    keyData.getConfig().getSnonce(),
                    keyData.getConfig().getAsalt(),
                    keyData.getConfig().getSbox(),
                    keyData.getConfig().getArgonOptions(),
                    keyData.getConfig().getPassword()
                ),
                PrivateKeyType.LOCKED
            );

        } else {
            privateKey = keyData.getConfig();
        }

        return JaxbUtil.marshalToString(privateKey);

    }

}
