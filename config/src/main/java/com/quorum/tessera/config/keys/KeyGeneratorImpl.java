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
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

import static com.quorum.tessera.config.PrivateKeyType.LOCKED;
import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyGeneratorImpl implements KeyGenerator {

    private final NaclFacade nacl;

    private final KeyEncryptor keyEncryptor;

    private final InputStream filenameStream;

    public KeyGeneratorImpl(final NaclFacade nacl, final KeyEncryptor keyEncryptor, final InputStream filenameStream) {
        this.nacl = Objects.requireNonNull(nacl);
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
        this.filenameStream = Objects.requireNonNull(filenameStream);
    }

    @Override
    public KeyData generate(final KeyDataConfig keyData) {

        final KeyPair generated = nacl.generateNewKeys();

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());

        final KeyData finalKeys;

        if (keyData.getType() == PrivateKeyType.LOCKED) {

            final KeyConfig encryptedPrivateKey = keyEncryptor.encryptPrivateKey(generated.getPrivateKey(), keyData.getPassword());

            finalKeys = new KeyData(
                new KeyDataConfig(
                    new PrivateKeyData(
                        generated.getPrivateKey().toString(),
                        new String(encryptedPrivateKey.getSnonce(), UTF_8),
                        new String(encryptedPrivateKey.getAsalt(), UTF_8),
                        new String(encryptedPrivateKey.getSbox(), UTF_8),
                        new ArgonOptions(
                            encryptedPrivateKey.getArgonOptions().getAlgorithm(),
                            encryptedPrivateKey.getArgonOptions().getIterations(),
                            encryptedPrivateKey.getArgonOptions().getMemory(),
                            encryptedPrivateKey.getArgonOptions().getParallelism()
                        ),
                        keyData.getPassword()
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

        System.out.println("Enter a relative or absolute path (without extension) to save the keys to");
        System.out.println("or leave blank to not save to separate file:");
        final String path = new Scanner(filenameStream).nextLine();

        if (!path.trim().isEmpty()) {

            final Path resolvedPath = Paths.get(path).toAbsolutePath();
            final Path parentPath = resolvedPath.getParent();
            final String filename = resolvedPath.getFileName().toString();

            final Path publicKeyPath = parentPath.resolve(filename + ".pub");
            final Path privateKeyPath = parentPath.resolve(filename + ".key");

            final String privateKeyJson = this.privateKeyToJson(finalKeys);

            IOCallback.execute(() -> Files.write(publicKeyPath, publicKeyBase64.getBytes(UTF_8)));
            IOCallback.execute(() -> Files.write(privateKeyPath, privateKeyJson.getBytes(UTF_8)));
        }

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
