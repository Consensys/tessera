package com.github.nexus.keygen;

import com.github.nexus.config.KeyData;
import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.PrivateKeyType;
import com.github.nexus.keyenc.KeyConfig;
import com.github.nexus.keyenc.KeyEncryptor;
import com.github.nexus.nacl.KeyPair;
import com.github.nexus.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Objects;

public class KeyGeneratorImpl implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyGeneratorImpl.class);

    private final NaclFacade nacl;

    private final KeyEncryptor keyEncryptor;

    public KeyGeneratorImpl(final NaclFacade nacl, final KeyEncryptor keyEncryptor) {
        this.nacl = Objects.requireNonNull(nacl);
        this.keyEncryptor = Objects.requireNonNull(keyEncryptor);
    }

    @Override
    public void generate(KeyData keyData) {

        final KeyPair generated = nacl.generateNewKeys();

        final PrivateKey privateKey = keyData.getPrivateKey();
        
        Objects.requireNonNull(privateKey.getPath(),"Private key path must be provided");
        
        final String privateKeyData;

        if (privateKey.getType() == PrivateKeyType.LOCKED) {

            final KeyConfig encryptedPrivateKey = keyEncryptor.encryptPrivateKey(
                generated.getPrivateKey(), privateKey.getPassword());

            privateKeyData = Json.createObjectBuilder()
                    .add("type", "argon2sbox")
                    .add("data", Json.createObjectBuilder()
                            .add("aopts", Json.createObjectBuilder()
                                    .add("variant", encryptedPrivateKey.getArgonOptions().getAlgorithm())
                                    .add("memory", encryptedPrivateKey.getArgonOptions().getMemory())
                                    .add("iterations", encryptedPrivateKey.getArgonOptions().getIterations())
                                    .add("parallelism", encryptedPrivateKey.getArgonOptions().getParallelism())
                            )
                            .add("snonce", new String(encryptedPrivateKey.getSnonce(), UTF_8))
                            .add("sbox", new String(encryptedPrivateKey.getSbox(), UTF_8))
                            .add("asalt", new String(encryptedPrivateKey.getAsalt(), UTF_8))
                    ).build()
                    .toString();

        } else {

            privateKeyData = Json.createObjectBuilder()
                    .add("type", "unlocked")
                    .add("data", Json.createObjectBuilder()
                            .add("bytes", generated.getPrivateKey().toString()))
                    .build()
                    .toString();
        }

        final String publicKeyBase64 = Base64.getEncoder().encodeToString(generated.getPublicKey().getKeyBytes());

        try {

            Files.write(keyData.getPrivateKey().getPath(),
                    privateKeyData.getBytes(UTF_8),
                    StandardOpenOption.CREATE_NEW);

//            Files.write(keyData.getPublicKey().getPath(),
//                    publicKeyBase64.getBytes(StandardCharsets.UTF_8),
//                    StandardOpenOption.CREATE_NEW);
            
        } catch (IOException ex) {
            throw new KeyGeneratorException(ex);
        }

    }

}
