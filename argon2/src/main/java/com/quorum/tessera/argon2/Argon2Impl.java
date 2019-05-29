package com.quorum.tessera.argon2;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Argon2Impl implements Argon2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(Argon2Impl.class);

    private static final ArgonOptions DEFAULT_OPTIONS = new ArgonOptions("i", 10, 1048576, 4);

    @Override
    public ArgonResult hash(final ArgonOptions options, final String password, final byte[] salt) {
        final int algorithm = this.getArgon2Instance(options.getAlgorithm());
        final char[] sanitisedPassword = (password == null) ? null : password.toCharArray();

        final Argon2Parameters parameters = new Argon2Parameters.Builder(algorithm)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(options.getIterations())
            .withMemoryAsKB(options.getMemory())
            .withParallelism(options.getParallelism())
            .withSalt(salt)
            .build();

        final Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(parameters);

        final byte[] hash = new byte[32];
        gen.generateBytes(sanitisedPassword, hash);

        LOGGER.debug("Argon2 hash produced the array {}", Arrays.toString(hash));

        return new ArgonResult(options, salt, hash);
    }

    @Override
    public ArgonResult hash(final String password, final byte[] salt) {
        return this.hash(DEFAULT_OPTIONS, password, salt);
    }

    /**
     * Finds the implementation specific value for a given algorithm type.
     *
     * @param algorithm the algorithm to use
     * @return the implementation specific integer representation
     */
    private int getArgon2Instance(final String algorithm) {
        LOGGER.debug("Searching for the Argon2 algorithm {}", algorithm);

        switch (algorithm) {
            case "d":
                return Argon2Parameters.ARGON2_d;
            case "id":
                return Argon2Parameters.ARGON2_id;
            case "i":
                return Argon2Parameters.ARGON2_i;
            default:
                throw new IllegalArgumentException("Invalid Argon2 algorithm " + algorithm);
        }
    }

}
