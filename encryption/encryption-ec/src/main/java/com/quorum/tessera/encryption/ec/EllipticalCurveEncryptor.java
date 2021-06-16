package com.quorum.tessera.encryption.ec;

import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.SharedKey;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EllipticalCurveEncryptor implements Encryptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EllipticalCurveEncryptor.class);

  private final int nonceLength;

  private final int sharedKeyLength;

  private final SecureRandom secureRandom;

  private final ECGenParameterSpec ecSpec;

  private final KeyFactory keyFactory;

  private final KeyPairGenerator keyPairGenerator;

  private final String symmetricCipher;

  public EllipticalCurveEncryptor(final String symmetricCipher, final String ellipticCurve) {
    this(symmetricCipher, ellipticCurve, 24, 32);
  }

  public EllipticalCurveEncryptor(
      final String symmetricCipher,
      final String ellipticCurve,
      int nonceLength,
      int sharedKeyLength) {
    this.nonceLength = nonceLength;
    this.sharedKeyLength = sharedKeyLength;
    this.symmetricCipher = symmetricCipher;
    secureRandom = new SecureRandom();
    try {
      ecSpec = new ECGenParameterSpec(ellipticCurve);
      keyFactory = KeyFactory.getInstance("EC");
      keyPairGenerator = KeyPairGenerator.getInstance("EC");
      keyPairGenerator.initialize(ecSpec, secureRandom);
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
      LOGGER.error("unable to initialize encryption facade", e);
      throw new EncryptorException("unable to initialize Encryptor");
    }
  }

  @Override
  public SharedKey computeSharedKey(PublicKey publicKey, PrivateKey privateKey) {
    try {
      KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");

      java.security.PrivateKey privKey =
          keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey.getKeyBytes()));

      keyAgreement.init(privKey);

      LOGGER.info("Encode public key {}", publicKey.encodeToBase64());

      X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKey.getKeyBytes());

      java.security.PublicKey pubKey = keyFactory.generatePublic(encodedKeySpec);

      keyAgreement.doPhase(pubKey, true);

      byte[] secret = keyAgreement.generateSecret();
      // for now ensure the secret is 32 bytes long (not sure if the keyAgreement secret length may
      // vary
      MessageDigest sha3256 = new SHA3.Digest256();
      final byte[] digest = sha3256.digest(secret);
      return SharedKey.from(digest);
    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
      LOGGER.error("unable to generate shared secret", e);
      throw new EncryptorException("unable to generate shared secret");
    }
  }

  @Override
  public byte[] seal(byte[] message, Nonce nonce, PublicKey publicKey, PrivateKey privateKey) {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte[] open(byte[] cipherText, Nonce nonce, PublicKey publicKey, PrivateKey privateKey) {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte[] sealAfterPrecomputation(byte[] message, Nonce nonce, SharedKey sharedKey) {
    try {
      Cipher cipher = Cipher.getInstance(this.symmetricCipher);
      cipher.init(
          Cipher.ENCRYPT_MODE,
          new SecretKeySpec(sharedKey.getKeyBytes(), "AES"),
          // does this mean that only 16 bytes from the nonce are being used?
          new GCMParameterSpec(128, nonce.getNonceBytes()));
      return cipher.doFinal(message);
    } catch (GeneralSecurityException e) {
      LOGGER.error("unable to perform symmetric encryption", e);
      throw new EncryptorException("unable to perform symmetric encryption");
    }
  }

  @Override
  public byte[] openAfterPrecomputation(byte[] cipherText, Nonce nonce, SharedKey sharedKey) {
    try {
      Cipher cipher = Cipher.getInstance(symmetricCipher);
      cipher.init(
          Cipher.DECRYPT_MODE,
          new SecretKeySpec(sharedKey.getKeyBytes(), "AES"),
          new GCMParameterSpec(128, nonce.getNonceBytes()));
      return cipher.doFinal(cipherText);
    } catch (GeneralSecurityException e) {
      LOGGER.error("unable to perform symmetric decryption", e);
      throw new EncryptorException("unable to perform symmetric decryption");
    }
  }

  @Override
  public Nonce randomNonce() {
    final byte[] nonceBytes = new byte[nonceLength];

    this.secureRandom.nextBytes(nonceBytes);

    final Nonce nonce = new Nonce(nonceBytes);

    LOGGER.debug("Generated random nonce {}", nonce);

    return nonce;
  }

  @Override
  public KeyPair generateNewKeys() {
    final java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
    return new KeyPair(
        PublicKey.from(keyToBytes(keyPair.getPublic())),
        PrivateKey.from(keyToBytes(keyPair.getPrivate())));
  }

  @Override
  public SharedKey createSingleKey() {
    LOGGER.debug("Generating random key");

    final byte[] keyBytes = new byte[sharedKeyLength];

    this.secureRandom.nextBytes(keyBytes);

    final SharedKey key = SharedKey.from(keyBytes);

    LOGGER.debug("Random key generated");
    LOGGER.debug("Generated key with value {}", key);

    return key;
  }

  private byte[] keyToBytes(java.security.PublicKey publicKey) {
    // this produces a 33 byte public key for the P-256 curve which then gets encoded to 44 chars as
    // base64 (just
    // like nacl)
    // ((BCECPublicKey)publicKey).getQ().getEncoded(true);
    // however for now we'll work with DER encoded public keys (x509)
    return publicKey.getEncoded();
  }

  private byte[] keyToBytes(java.security.PrivateKey privateKey) {
    return privateKey.getEncoded();
  }
}
