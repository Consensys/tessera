package com.github.nexus.argon2;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;

import java.util.stream.Stream;

public class Argon2Impl implements Argon2 {

    private static final ArgonOptions DEFAULT_OPTIONS = new ArgonOptions("i", 10, 1048576, 4);

    @Override
    public ArgonResult hash(final ArgonOptions options, final String password, final byte[] salt) {
        final Argon2Advanced argon2 = this.getArgon2Instance(options.getAlgorithm());

        final byte[] hash = argon2.rawHash(
            options.getIterations(),
            options.getMemory(),
            options.getParallelism(),
            password,
            salt
        );

        final String algoName = Stream.of("d", "id", "i")
            .filter(options.getAlgorithm()::equals)
            .findAny()
            .orElse("i");

        return new ArgonResult(
            new ArgonOptions(
                algoName,
                options.getIterations(),
                options.getMemory(),
                options.getParallelism()
            ),
            salt,
            hash
        );
    }

    @Override
    public ArgonResult hash(final String password, final byte[] salt) {
        return this.hash(DEFAULT_OPTIONS, password, salt);
    }

    private Argon2Advanced getArgon2Instance(final String algorithm) {
        switch (algorithm) {
            case "d":
                return Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2d);
            case "id":
                return Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2id);
            case "i":
            default:
                return Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2i);
        }
    }
}
