package com.quorum.tessera.encryption.nacl.jnacl;

import static com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305.*;

import com.neilalexander.jnacl.NaCl;
import com.quorum.tessera.encryption.*;
import java.security.SecureRandom;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Uses jnacl, which is a pure Java implementation of the NaCl standard */
public class Jnacl implements Encryptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Jnacl.class);

  private final SecureRandom secureRandom;

  private final SecretBox secretBox;

  public Jnacl(final SecureRandom secureRandom, final SecretBox secretBox) {
    this.secureRandom = Objects.requireNonNull(secureRandom);
    this.secretBox = Objects.requireNonNull(secretBox);
  }

  @Override
  public SharedKey computeSharedKey(final PublicKey publicKey, final PrivateKey privateKey) {
    final byte[] precomputed = new byte[crypto_secretbox_BEFORENMBYTES];

    LOGGER.debug(
        "Computing the shared key for public key {} and private key {}", publicKey, privateKey);
    final int jnaclResult =
        secretBox.cryptoBoxBeforenm(precomputed, publicKey.getKeyBytes(), privateKey.getKeyBytes());

    if (jnaclResult == -1) {
      LOGGER.error(
          "Could not compute the shared key for pub {} and priv {}", publicKey, privateKey);
      throw new EncryptorException("jnacl could not compute the shared key");
    }

    final SharedKey sharedKey = SharedKey.from(precomputed);

    LOGGER.debug("Computed shared key {} for pub {} and priv {}", sharedKey, publicKey, privateKey);

    return sharedKey;
  }

  @Override
  public byte[] seal(
      final byte[] message,
      final Nonce nonce,
      final PublicKey publicKey,
      final PrivateKey privateKey) {

    LOGGER.debug(
        "Sealing message using nonce {}, public key {} and private key {}",
        nonce,
        publicKey,
        privateKey);

    try {

      final NaCl nacl = new NaCl(privateKey.getKeyBytes(), publicKey.getKeyBytes());

      final byte[] cipherText = nacl.encrypt(message, nonce.getNonceBytes());

      LOGGER.debug(
          "Created sealed payload using nonce {}, public key {} and private key {}",
          nonce,
          publicKey,
          privateKey);

      return extract(cipherText, crypto_secretbox_BOXZEROBYTES);

    } catch (final Exception ex) {
      throw new EncryptorException(ex.getMessage());
    }
  }

  @Override
  public byte[] open(
      final byte[] cipherText,
      final Nonce nonce,
      final PublicKey publicKey,
      final PrivateKey privateKey) {
    LOGGER.debug(
        "Opening message using nonce {}, public key {} and private key {}",
        nonce,
        publicKey,
        privateKey);

    try {

      final byte[] paddedInput = pad(cipherText, crypto_secretbox_BOXZEROBYTES);

      final NaCl nacl = new NaCl(privateKey.getKeyBytes(), publicKey.getKeyBytes());

      final byte[] plaintext = nacl.decrypt(paddedInput, nonce.getNonceBytes());

      LOGGER.debug(
          "Opened message using nonce {}, public key {} and private key {}",
          nonce,
          publicKey,
          privateKey);

      return plaintext;
    } catch (final Exception ex) {
      throw new EncryptorException(ex.getMessage());
    }
  }

  @Override
  public byte[] sealAfterPrecomputation(
      final byte[] message, final Nonce nonce, final SharedKey sharedKey) {

    final byte[] paddedMessage = new byte[message.length + crypto_secretbox_ZEROBYTES];
    final byte[] output = new byte[message.length + crypto_secretbox_ZEROBYTES];

    LOGGER.debug("Sealing message using nonce {} and shared key {}", nonce, sharedKey);

    System.arraycopy(message, 0, paddedMessage, crypto_secretbox_ZEROBYTES, message.length);
    final int jnaclResult =
        secretBox.cryptoBoxAfternm(
            output,
            paddedMessage,
            paddedMessage.length,
            nonce.getNonceBytes(),
            sharedKey.getKeyBytes());

    if (jnaclResult == -1) {
      LOGGER.error("Could not create sealed payload using shared key {}", sharedKey);
      throw new EncryptorException("jnacl could not seal the payload using the shared key");
    }

    LOGGER.debug("Created sealed payload using nonce {} and shared key {}", nonce, sharedKey);

    return extract(output, crypto_secretbox_BOXZEROBYTES);
  }

  @Override
  public byte[] openAfterPrecomputation(
      final byte[] cipherText, final Nonce nonce, final SharedKey sharedKey) {
    LOGGER.debug("Opening message using nonce {} and shared key {}", nonce, sharedKey);

    final byte[] paddedInput = pad(cipherText, crypto_secretbox_BOXZEROBYTES);
    final byte[] paddedOutput = new byte[paddedInput.length];

    final int jnaclResult =
        secretBox.cryptoBoxOpenAfternm(
            paddedOutput,
            paddedInput,
            paddedInput.length,
            nonce.getNonceBytes(),
            sharedKey.getKeyBytes());

    if (jnaclResult == -1) {
      LOGGER.error("Could not open sealed payload using shared key {}", sharedKey);
      throw new EncryptorException("jnacl could not open the payload using the shared key");
    }

    LOGGER.debug("Opened sealed payload for shared key {}", sharedKey);
    LOGGER.debug("Opened payload using nonce {} and shared key {}", nonce, sharedKey);

    return extract(paddedOutput, crypto_secretbox_ZEROBYTES);
  }

  @Override
  public Nonce randomNonce() {
    final byte[] nonceBytes = new byte[crypto_secretbox_NONCEBYTES];

    this.secureRandom.nextBytes(nonceBytes);

    final Nonce nonce = new Nonce(nonceBytes);

    LOGGER.debug("Generated random nonce {}", nonce);

    return nonce;
  }

  @Override
  public KeyPair generateNewKeys() {
    final byte[] publicKey = new byte[crypto_secretbox_PUBLICKEYBYTES];
    final byte[] privateKey = new byte[crypto_secretbox_SECRETKEYBYTES];

    LOGGER.info("Generating new keypair...");

    final int jnaclResult = secretBox.cryptoBoxKeypair(publicKey, privateKey);

    if (jnaclResult == -1) {
      LOGGER.error("Unable to generate a new keypair!");
      throw new EncryptorException("jnacl could not generate a new public/private keypair");
    }

    final PublicKey pubKey = PublicKey.from(publicKey);
    final PrivateKey privKey = PrivateKey.from(privateKey);

    LOGGER.info("Generated new key pair with public key {}", pubKey);
    LOGGER.debug("Generated public key {} and private key {}", pubKey, privKey);

    return new KeyPair(pubKey, privKey);
  }

  @Override
  public SharedKey createSingleKey() {
    LOGGER.debug("Generating random key");

    final byte[] keyBytes = new byte[crypto_secretbox_PUBLICKEYBYTES];

    this.secureRandom.nextBytes(keyBytes);

    final SharedKey key = SharedKey.from(keyBytes);

    LOGGER.debug("Random key generated");
    LOGGER.debug("Generated key with value {}", key);

    return key;
  }

  /**
   * Left-pads a given message with padSize amount of zeros
   *
   * @param input the message to be padded
   * @param padSize the amount of left-padding to apply
   * @return the padded message
   */
  private byte[] pad(final byte[] input, final int padSize) {
    final byte[] paddedMessage = new byte[padSize + input.length];
    System.arraycopy(input, 0, paddedMessage, padSize, input.length);

    return paddedMessage;
  }

  /**
   * Removes left-padding from a given message to tune of padSize
   *
   * @param input The message from which to remove left-padding
   * @param padSize The amount of left-padding to remove
   * @return The trimmed message
   */
  private byte[] extract(final byte[] input, final int padSize) {
    final byte[] extractedMessage = new byte[input.length - padSize];
    System.arraycopy(input, padSize, extractedMessage, 0, extractedMessage.length);

    return extractedMessage;
  }
}
