package com.github.nexus.key;

import com.github.nexus.argon2.Argon2;
import com.github.nexus.argon2.ArgonResult;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.Nonce;
import com.github.nexus.util.Base64Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * An implementation of {@link KeyEncryptor} that uses Argon2
 *
 * The password is hashed using the generated/provided salt to generate a 32 byte hash
 * This hash is then used as the symmetric key to encrypt the private key
 */
public class KeyEncryptorImpl implements KeyEncryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyEncryptorImpl.class);

    private final Argon2 argon2;

    private final NaclFacade nacl;

    private final Base64Decoder decoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public KeyEncryptorImpl(final Argon2 argon2, final NaclFacade nacl, final Base64Decoder decoder) {
        this.argon2 = Objects.requireNonNull(argon2);
        this.nacl = Objects.requireNonNull(nacl);
        this.decoder = Objects.requireNonNull(decoder);
    }

    @Override
    public JsonObject encryptPrivateKey(final Key privateKey, final String password) {

        LOGGER.info("Encrypting a private key");

        LOGGER.debug("Encrypting private key {} using password {}", privateKey, password);

        final byte[] salt = new byte[KeyEncryptor.SALTLENGTH];
        secureRandom.nextBytes(salt);

        LOGGER.debug("Generated the random salt {}", Arrays.toString(salt));

        final ArgonResult argonResult = argon2.hash(password, salt);

        final Nonce nonce = nacl.randomNonce();
        LOGGER.debug("Generated the random nonce {}", nonce);

        final byte[] encryptedKey = nacl.sealAfterPrecomputation(
            privateKey.getKeyBytes(),
            nonce,
            new Key(argonResult.getHash())
        );

        final EncryptedPrivateKey encryptedPrivateKey = new EncryptedPrivateKey(
            argonResult.getOptions(),
            decoder.encodeToString(nonce.getNonceBytes()),
            decoder.encodeToString(salt),
            decoder.encodeToString(encryptedKey)
        );

        LOGGER.info("Private key encrypted");

        return EncryptedPrivateKey.to(encryptedPrivateKey);

    }

    @Override
    public Key decryptPrivateKey(final JsonObject encryptedKey, final String password) {

        LOGGER.info("Decrypting private key");
        LOGGER.debug("Decrypting private key {} using password {}", encryptedKey.toString(), password);

        final EncryptedPrivateKey encryptedPrivateKey = EncryptedPrivateKey.from(encryptedKey);

        final byte[] salt = decoder.decode(encryptedPrivateKey.getAsalt());

        final ArgonResult argonResult = argon2.hash(encryptedPrivateKey.getAopts(), password, salt);

        final byte[] originalKey = nacl.openAfterPrecomputation(
            decoder.decode(encryptedPrivateKey.getSbox()),
            new Nonce(decoder.decode(encryptedPrivateKey.getSnonce())),
            new Key(argonResult.getHash())
        );

        LOGGER.info("Decrypting private key");
        LOGGER.debug("Decrypted private key {}", new Key(originalKey));

        return new Key(originalKey);
    }

}
