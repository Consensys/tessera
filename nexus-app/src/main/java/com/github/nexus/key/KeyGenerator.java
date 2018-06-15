package com.github.nexus.key;

import com.github.nexus.nacl.KeyPair;

public interface KeyGenerator {

    /**
     * Generates a new keypair and saves it to a file using the given name
     * The public key has extension {@code .pub} and the private key has extension {@code .key}
     * <p>
     * Encrypts the key by generating a hash using Argon2 of the provided password
     *
     * @param name the name of the files to save
     * @return the {@link KeyPair} of generated public/private keys
     * @throws RuntimeException if the keys could not be written to file
     */
    KeyPair generateNewKeys(String name, String password);

    /**
     * Generates a new keypair and saves it to a file using the given name
     * The public key has extension {@code .pub} and the private key has extension {@code .key}
     *
     * @param name the name of the files to save
     * @return the {@link KeyPair} of generated public/private keys
     * @throws RuntimeException if the keys could not be written to file
     */
    KeyPair generateNewKeys(String name);

}
