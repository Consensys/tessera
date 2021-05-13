package com.quorum.tessera.config.keys;

import com.quorum.tessera.argon2.Argon2;
import com.quorum.tessera.argon2.ArgonResult;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.SharedKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link KeyEncryptor} that uses Argon2
 *
 * <p>The password is hashed using the generated/provided salt to generate a 32 byte hash This hash
 * is then used as the symmetric key to encrypt the private key
 */
public class KeyEncryptorImpl implements KeyEncryptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyEncryptorImpl.class);

  private final Argon2 argon2;

  private final Encryptor encryptor;

  private final Base64.Decoder decoder = Base64.getDecoder();

  private final Base64.Encoder encoder = Base64.getEncoder();

  private final SecureRandom secureRandom = new SecureRandom();

  public KeyEncryptorImpl(final Argon2 argon2, final Encryptor encryptor) {
    this.argon2 = Objects.requireNonNull(argon2);
    this.encryptor = Objects.requireNonNull(encryptor);
  }

  @Override
  public PrivateKeyData encryptPrivateKey(
      final PrivateKey privateKey, final char[] password, final ArgonOptions argonOptions) {

    LOGGER.info("Encrypting a private key");

    LOGGER.debug("Encrypting private key {} using password {}", privateKey, password);

    final byte[] salt = new byte[KeyEncryptor.SALTLENGTH];
    this.secureRandom.nextBytes(salt);

    LOGGER.debug("Generated the random salt {}", Arrays.toString(salt));

    final ArgonResult argonResult;

    if (argonOptions == null) {
      argonResult = this.argon2.hash(password, salt);
    } else {
      argonResult =
          this.argon2.hash(
              new com.quorum.tessera.argon2.ArgonOptions(
                  argonOptions.getAlgorithm(),
                  argonOptions.getIterations(),
                  argonOptions.getMemory(),
                  argonOptions.getParallelism()),
              password,
              salt);
    }

    final Nonce nonce = this.encryptor.randomNonce();
    LOGGER.debug("Generated the random nonce {}", nonce);

    final byte[] encryptedKey =
        this.encryptor.sealAfterPrecomputation(
            privateKey.getKeyBytes(), nonce, SharedKey.from(argonResult.getHash()));

    LOGGER.info("Private key encrypted");

    final String snonce = this.encoder.encodeToString(nonce.getNonceBytes());
    final String asalt = this.encoder.encodeToString(salt);
    final String sbox = this.encoder.encodeToString(encryptedKey);

    return new PrivateKeyData(
        null,
        snonce,
        asalt,
        sbox,
        new ArgonOptions(
            argonResult.getOptions().getAlgorithm(),
            argonResult.getOptions().getIterations(),
            argonResult.getOptions().getMemory(),
            argonResult.getOptions().getParallelism()));
  }

  @Override
  public PrivateKey decryptPrivateKey(final PrivateKeyData privateKey, final char[] password) {

    LOGGER.info("Decrypting private key");
    LOGGER.debug("Decrypting private key {} using password {}", privateKey.getValue(), password);

    final byte[] salt = this.decoder.decode(privateKey.getAsalt());

    final ArgonResult argonResult =
        this.argon2.hash(
            new com.quorum.tessera.argon2.ArgonOptions(
                privateKey.getArgonOptions().getAlgorithm(),
                privateKey.getArgonOptions().getIterations(),
                privateKey.getArgonOptions().getMemory(),
                privateKey.getArgonOptions().getParallelism()),
            password,
            salt);

    final byte[] originalKey =
        this.encryptor.openAfterPrecomputation(
            this.decoder.decode(privateKey.getSbox()),
            new Nonce(this.decoder.decode(privateKey.getSnonce())),
            SharedKey.from(argonResult.getHash()));

    PrivateKey outcome = PrivateKey.from(originalKey);

    LOGGER.info("Decrypted private key");
    LOGGER.debug("Decrypted private key {}", outcome.encodeToBase64());

    return outcome;
  }
}
