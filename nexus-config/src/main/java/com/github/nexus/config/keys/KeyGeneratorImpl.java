package com.github.nexus.config.keys;

import com.github.nexus.config.*;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Objects;

import static com.github.nexus.config.PrivateKeyType.LOCKED;
import static com.github.nexus.config.PrivateKeyType.UNLOCKED;
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
    public KeyData generate(KeyDataConfig keyData) {

        final KeyPair generated = nacl.generateNewKeys();

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());

        if (keyData.getType() == PrivateKeyType.LOCKED) {

            final KeyConfig encryptedPrivateKey = keyEncryptor.encryptPrivateKey(generated.getPrivateKey(), keyData.getPassword());

            return new KeyData(
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
                    LOCKED
                ),
                generated.getPrivateKey().toString(),
                publicKeyBase64
            );

        } else {

            return new KeyData(
                new KeyDataConfig(
                    new PrivateKeyData(generated.getPrivateKey().toString(), null, null, null, null, null),
                    UNLOCKED
                ),
                generated.getPrivateKey().toString(),
                publicKeyBase64
            );

        }

    }

}
