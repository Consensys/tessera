package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.IOCallback;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.NaclFacade;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

import static com.quorum.tessera.config.PrivateKeyType.LOCKED;
import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyGeneratorImpl implements KeyGenerator {

    private static final String EMPTY_FILENAME = "";

    private final NaclFacade nacl;

    private final KeyEncryptor keyEncryptor;

    private final InputStream passwordStream;

    public KeyGeneratorImpl(final NaclFacade nacl, final KeyEncryptor keyEncryptor, final InputStream passwordStream) {
        this.nacl = Objects.requireNonNull(nacl);
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
        this.passwordStream = Objects.requireNonNull(passwordStream);
    }

    @Override
    public KeyData generate(final String filename) {

        System.out.println("Enter a password if you want to lock the private key or leave blank");

        String password = new Scanner(passwordStream).nextLine();

        final KeyPair generated = this.nacl.generateNewKeys();

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());

        final KeyData finalKeys;

        if (!password.isEmpty()) {

            final PrivateKeyData encryptedPrivateKey = this.keyEncryptor.encryptPrivateKey(
                generated.getPrivateKey(), password
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
                null
            );

        } else {

            finalKeys = new KeyData(
                new KeyDataConfig(
                    new PrivateKeyData(generated.getPrivateKey().toString(), null, null, null, null, null),
                    UNLOCKED
                ),
                generated.getPrivateKey().toString(),
                publicKeyBase64,
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

        return finalKeys;
    }

    private String privateKeyToJson(final KeyData keyData) {

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final KeyDataConfig privateKey;

        if (LOCKED.equals(keyData.getConfig().getType())) {

            privateKey = new KeyDataConfig(
                new PrivateKeyData(
                    null,
                    keyData.getConfig().getSnonce(),
                    keyData.getConfig().getAsalt(),
                    keyData.getConfig().getSbox(),
                    keyData.getConfig().getArgonOptions(),
                    null
                ),
                LOCKED
            );

        } else {
            privateKey = keyData.getConfig();
        }

        JaxbUtil.marshal(privateKey, outputStream);

        return new String(outputStream.toByteArray());

    }

}
