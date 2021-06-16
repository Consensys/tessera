package com.quorum.tessera.encryption.nacl.jnacl;

import com.neilalexander.jnacl.crypto.curve25519xsalsa20poly1305;

/**
 * An interface to make testing of the static methods provided by {@link curve25519xsalsa20poly1305}
 * easier.
 */
public interface SecretBox {

  /**
   * Computes a shared key between the given public and private key
   *
   * @param output The output array that contains the shared key
   * @param publicKey The bytes of the public key
   * @param privateKey The bytes of the private key
   * @return Whether the operation was successful (0 for success, -1 for failure)
   */
  int cryptoBoxBeforenm(byte[] output, byte[] publicKey, byte[] privateKey);

  /**
   * Seal a message into ciphertext using a shared key
   *
   * @param output the output ciphertext that contains the required 0 padding from NaCL
   * @param message the input message with the require 0 padding from NaCL
   * @param messageLength the length of the padding input message
   * @param nonce a unique nonce to encrypt the message with
   * @param sharedKey the shared key to encrypt the message with
   * @return Whether the operation was successful (0 for success, -1 for failure)
   */
  int cryptoBoxAfternm(
      byte[] output, byte[] message, int messageLength, byte[] nonce, byte[] sharedKey);

  /**
   * Opens a sealed box using the provided shared key
   *
   * @param output the output array for the unencrpyted message
   * @param message the cipher text with the require 0 padding from NaCL
   * @param messageLength the length of the message + the required 0 padding from NaCL
   * @param nonce the nonce that was used to seal the box
   * @param sharedKey the shared key
   * @return Whether the operation was successful (0 for success, -1 for failure)
   */
  int cryptoBoxOpenAfternm(
      byte[] output, byte[] message, int messageLength, byte[] nonce, byte[] sharedKey);

  /**
   * Generates a new keypair The input arrays must be at least 32 bytes long (and only the first 32
   * bytes are populated if the array is longer)
   *
   * @param publicKey The output array for the public key
   * @param privateKey The output array for the private key
   * @return Whether the operation was successful (0 for success, -1 for failure)
   */
  int cryptoBoxKeypair(byte[] publicKey, byte[] privateKey);
}
