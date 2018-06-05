package com.github.nexus.enclave.keys;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.keys.model.KeyPair;

import java.nio.file.Path;

/**
 * Manages local keys for the running node, include key lookups and key loading/generation
 */
public interface KeyManager {

    /**
     * Fetches the public key that corresponds to the given private key
     * This means that the returned public key is under control of the current node
     * (i.e. it is one of our keys)
     *
     * @param privateKey the private key for which to find the public key
     * @return the corresponding public key
     * @throws RuntimeException if the private key could not be found
     */
    Key getPublicKeyForPrivateKey(Key privateKey);

    /**
     * Fetches the private key that corresponds to the given public key
     * This means that the returned private key is under control of the current node
     * (i.e. it is one of our keys)
     *
     * @param publicKey the public key for which to find the private key
     * @return the corresponding private key
     * @throws RuntimeException if the public key could not be found
     */
    Key getPrivateKeyForPublicKey(Key publicKey);

    /**
     * Generates a new keypair and saves it to a file using the given name
     * The public key has extension {@code .pub} and the private key has extension {@code .key}
     * The folder in which the keys are saved is up to the implementor
     *
     * @param name the name of the files to save
     * @return the {@link KeyPair} of generated public/private keys
     * @throws RuntimeException if the keys could not be written to file
     */
    KeyPair generateNewKeys(String name);

    /**
     * Loads the specified keys from file
     *
     * @param publicKeyPath  The path to the public key
     * @param privateKeyPath The path to the private key
     * @return the loaded {@link KeyPair}
     * @throws RuntimeException if the keys could not be found
     */
    KeyPair loadKeypair(Path publicKeyPath, Path privateKeyPath);

}
