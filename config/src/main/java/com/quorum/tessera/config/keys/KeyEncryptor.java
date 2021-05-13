package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.encryption.PrivateKey;

/**
 * Provides key encryption and decryption for private keys into the representation expected by a
 * private key file
 */
public interface KeyEncryptor {

  int SALTLENGTH = 16;

  /**
   * Encrypts a private key using the given password and returns a JSON object that can be
   * interpreted when decrypting
   *
   * @param privateKey the key to encrypt
   * @param password the password to encrypt the key with
   * @return the configuration that can be used to decrypt the private key
   */
  PrivateKeyData encryptPrivateKey(
      PrivateKey privateKey, char[] password, ArgonOptions argonOptions);

  /**
   * Decrypts a private key using the password and information provided by the given JSON object.
   * What information the object contains is up to the implementor.
   *
   * @param privateKeyConfig the configuration used to decrypt the private key
   * @param password the password that should be used to decrypt the private key
   * @return the decrypted private key
   */
  PrivateKey decryptPrivateKey(PrivateKeyData privateKeyConfig, char[] password);
}
