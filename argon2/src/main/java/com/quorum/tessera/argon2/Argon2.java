package com.quorum.tessera.argon2;

import java.util.ServiceLoader;

/**
 * Provides hashing functions using the Argon2 algorithms
 * Validation of various inputs is left to the implementor, although
 * the validation should be consistent amongst implementations.
 * (i.e. the same inputs should work for any implementation)
 */
public interface Argon2 {

    /**
     * Hash the password using the provided options
     *
     * @param options the custom options to call Argon2 with
     * @param password the password to hash
     * @param salt the salt to apply when hashing
     * @return the result which contains the output, as well as the input parameters
     */
    ArgonResult hash(ArgonOptions options, String password, byte[] salt);

    /**
     * Hash the password using the given salt and some default options
     *
     * @param password the password to hash
     * @param salt the salt to apply when hashing
     * @return the result which contains the output, as well as the input parameters
     */
    ArgonResult hash(String password, byte[] salt);

    static Argon2 create() {
        return ServiceLoader.load(Argon2.class).iterator().next();
    }

}
