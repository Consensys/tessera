package com.github.nexus.keygen;

import com.github.nexus.nacl.KeyPair;

import java.io.InputStream;

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
    Pair<String, String> generateNewKeys(String name, String password);

    /**
     * Generates a new keypair and saves it to a file using the given name
     * The public key has extension {@code .pub} and the private key has extension {@code .key}
     *
     * @param name the name of the files to save
     * @return the {@link KeyPair} of generated public/private keys
     * @throws RuntimeException if the keys could not be written to file
     */
    Pair<String, String> generateNewKeys(String name);

    /**
     * Suuplies prompts to stdin and stdout for a password to use and whether
     * to save to file or not
     *
     * @param name
     */
    void promptForGeneration(String name, InputStream input);

    class Pair<T, U> {

        public final T left;

        public final U right;

        public Pair(final T left, final U right) {
            this.left = left;
            this.right = right;
        }
    }

}
