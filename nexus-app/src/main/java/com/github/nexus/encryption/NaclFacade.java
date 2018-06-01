package com.github.nexus.encryption;

/**
 * An facade against a particular implementation of NaCL libraries
 * <p>
 * Terminology is kept in line with the NaCL binding found at
 * http://nacl.cr.yp.to/index.html
 */
public interface NaclFacade {

    /**
     * Compute the shared key from a public/private key combination
     * The keys must be from different keysets.
     * Providing the public key for the corresponding private key (and vice versa) results in an error
     * <p>
     * The shared key for a public/private key combo is the same as if the private/public corresponding keys
     * were provided.
     * i.e. public1/private2 == private1/public2
     *
     * @param keyOne A public or private key from the first keyset
     * @param keyTwo A public or private key from the second keyset
     * @return The shared key for this key pair.
     */
    byte[] computeSharedKey(byte[] keyOne, byte[] keyTwo);

    /**
     * Encrypt a payload directly using the given public/private key pair for the sender/recipient
     *
     * @param message    The payload to be encrypted
     * @param nonce      A unique nonce for this public/private pair
     * @param publicKey  The key from either sender or recipient
     * @param privateKey The other key from either sender or recipient
     * @return The encrypted payload
     */
    byte[] seal(byte[] message, byte[] nonce, byte[] publicKey, byte[] privateKey);

    /**
     * Decrypt a payload directly using the given public/private key pair for the sender/recipient
     *
     * @param cipherText The payload to be encrypted
     * @param nonce      A unique nonce for this public/private pair
     * @param publicKey  The key from either sender or recipient
     * @param privateKey The other key from either sender or recipient
     * @return The encrypted payload
     */
    byte[] open(byte[] cipherText, byte[] nonce, byte[] publicKey, byte[] privateKey);

    /**
     * Encrypt a payload using the given public/private key pair for the sender/recipient
     * This computes the shared key and then calls {@see NaclFacade#sealAfterPrecomputation}
     *
     * @param message   The payload to be encrypted
     * @param nonce     A unique nonce for this public/private pair
     * @param sharedKey The shared key between the sender and recipient of the payload
     * @return The encrypted payload
     */
    byte[] sealAfterPrecomputation(byte[] message, byte[] nonce, byte[] sharedKey);

    /**
     * Decrypts a payload using the shared key between the sender and recipient
     *
     * @param input     The encrypted payload
     * @param nonce     The nonce that was used to encrypt this payload
     * @param sharedKey The shared key for the sender and recipient
     * @return The decrypted payload
     */
    byte[] openAfterPrecomputation(byte[] input, byte[] nonce, byte[] sharedKey);

    /**
     * Generates a new random nonce of the correct size
     *
     * @return a byte array containing random data to be used as a nonce
     */
    byte[] randomNonce();

    /**
     * Generates a new public and private keypair
     *
     * @return A pair of public and private keys
     */
    KeyPair generateNewKeys();

}