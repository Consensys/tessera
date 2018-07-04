package com.github.nexus.keyenc;

import com.github.nexus.config.*;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyGeneratorImpl implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyGeneratorImpl.class);

    private final NaclFacade nacl;

    private final KeyEncryptor keyEncryptor;

    public KeyGeneratorImpl(final NaclFacade nacl, final KeyEncryptor keyEncryptor) {
        this.nacl = Objects.requireNonNull(nacl);
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
    }

    @Override
    public KeyData generate(final KeyData keyData) {

        final KeyPair generated = nacl.generateNewKeys();

        final PrivateKey privateKey = keyData.getPrivateKey();
        
        final PrivateKey privateKeyData;

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());

        if (privateKey.getType() == PrivateKeyType.LOCKED) {

            final KeyConfig encryptedPrivateKey
                = keyEncryptor.encryptPrivateKey(generated.getPrivateKey(), privateKey.getPassword());

            privateKeyData = new PrivateKey(
                new PrivateKeyData(
                    null,
                    new String(encryptedPrivateKey.getSnonce(), UTF_8),
                    new String(encryptedPrivateKey.getAsalt(), UTF_8),
                    new String(encryptedPrivateKey.getSbox(), UTF_8),
                    new ArgonOptions(
                        encryptedPrivateKey.getArgonOptions().getAlgorithm(),
                        encryptedPrivateKey.getArgonOptions().getIterations(),
                        encryptedPrivateKey.getArgonOptions().getMemory(),
                        encryptedPrivateKey.getArgonOptions().getParallelism()
                    ),
                    privateKey.getPassword()
                ),
                PrivateKeyType.LOCKED
            );

        } else {

            privateKeyData = new PrivateKey(
                new PrivateKeyData(generated.getPrivateKey().toString(), null, null, null, null, null),
                PrivateKeyType.UNLOCKED
            );

        }

        return new KeyData(privateKeyData, publicKeyBase64);
    }

}
