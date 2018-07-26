package com.quorum.tessera.nacl;

/**
 * The API provided to the application that all implementation of this API
 * module should extend
 *
 * Provides all function relating to encrypting and decrypting messages
 * using public/private and symmetric keys.
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
     * @param publicKey  A public key from the first keyset
     * @param privateKey A private key from the second keyset
     * @return The shared key for this key pair.
     */
    Key computeSharedKey(Key publicKey, Key privateKey);

    /**
     * Encrypt a payload directly using the given public/private key pair for the sender/recipient
     *
     * @param message    The payload to be encrypted
     * @param nonce      A unique nonce for this public/private pair
     * @param publicKey  The key from either sender or recipient
     * @param privateKey The other key from either sender or recipient
     * @return The encrypted payload
     */
    byte[] seal(byte[] message, Nonce nonce, Key publicKey, Key privateKey);

    /**
     * Decrypt a payload directly using the given public/private key pair for the sender/recipient
     *
     * @param cipherText The payload to be encrypted
     * @param nonce      A unique nonce for this public/private pair
     * @param publicKey  The key from either sender or recipient
     * @param privateKey The other key from either sender or recipient
     * @return The encrypted payload
     */
    byte[] open(byte[] cipherText, Nonce nonce, Key publicKey, Key privateKey);

    /**
     * Encrypt a payload using the given public/private key pair for the sender/recipient
     *
     * @param message   The payload to be encrypted
     * @param nonce     A unique nonce for this public/private pair
     * @param sharedKey The shared key between the sender and recipient of the payload
     * @return The encrypted payload
     */
    byte[] sealAfterPrecomputation(byte[] message, Nonce nonce, Key sharedKey);

    /**
     * Decrypts a payload using the shared key between the sender and recipient
     *
     * @param cipherText The encrypted payload
     * @param nonce      The nonce that was used to encrypt this payload
     * @param sharedKey  The shared key for the sender and recipient
     * @return The decrypted payload
     */
    byte[] openAfterPrecomputation(byte[] cipherText, Nonce nonce, Key sharedKey);

    /**
     * Generates a new random nonce of the correct size
     *
     * @return a {@link Nonce} containing random data to be used as a nonce
     */
    Nonce randomNonce();

    /**
     * Generates a new public and private keypair
     *
     * @return A pair of public and private keys
     */
    KeyPair generateNewKeys();

    /**
     * Creates a single standalone key
     *
     * @return The randomly generated key
     */
    Key createSingleKey();

}
